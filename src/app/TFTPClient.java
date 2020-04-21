package app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.spec.DSAGenParameterSpec;
import java.util.Arrays;
import java.util.stream.Stream;

public class TFTPClient {
    // Server configurations
    private String SERVER_IP = "";
    private final int DEFAULT_SERVER_PORT = 69;

    // OP codes
    private byte OP_RRQ = 1;
    private byte OP_WRQ = 2;
    private final byte OP_DATA = 3;
    private final byte OP_ACK = 4;
    private final byte OP_ERROR = 5;

    // Data modes
    private final String MODE_NETASCII = "netascii";
    private final String MODE_OCTET = "octet";
    private final String MODE_MAIL = "mail";

    // Networking inforamtion
    private InetAddress ipAddress = null;
    private DatagramSocket socket = null;
    private DatagramPacket outBoundPacket = null;
    private DatagramPacket inBoundPacket = null;

    private ByteArrayOutputStream ret = null;
    private OutputStream stream = null;
    private FileInputStream fis = null;

    // other
    private final static int DATAGRAM_MAX_SIZE = 516;
    private byte[] request = null;
    private byte[] buffer = null;
    private byte zeroConst = 0;

    private short blockNum;

    public TFTPClient(final String server_ip) throws UnknownHostException, SocketException {
        SERVER_IP = server_ip;
        ipAddress = InetAddress.getByName(SERVER_IP);
        socket = new DatagramSocket();
    }

    // TODO: in both getFile and putFile we need to ask the user how they want to send their data
    // whether it's in octet, netascii, or mail
    // then we format the request to send

    // TODO: read up on the stuff pieces we found to understand what Dr. C was talking about
    // why do we need to handle cardinal integers?

    public void getFile(final String file) throws IOException {
        System.out.println("Beginning request to get " + file + " from server.");
        // create read request and send packet
        request = formatRequestWritePacket(OP_RRQ, file, MODE_OCTET);
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        socket.send(outBoundPacket);
        // receive file and write to disc
        ByteArrayOutputStream inData = receiveFile();
        writeToDisc(inData, file);
        inData.close(); 
        System.out.println("Terminating connection with server.");
    }

    public void putFile(final String file) throws IOException {
        blockNum = (short) 1;
        // load file
        fis = new FileInputStream(file);
        System.out.println("Beginning request to write " + file + " to server.");
        // create write request and send packet
        request = formatRequestWritePacket(OP_WRQ, file, MODE_OCTET);
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        socket.send(outBoundPacket);
        // get ack
        byte[] startReceive = {(byte) 0, (byte) 0};
        if (receiveAck(startReceive)) {
            sendFile();
        }
        System.out.println("Terminating connection with server.");
    }

    private ByteArrayOutputStream receiveFile() throws IOException {
        ret = new ByteArrayOutputStream();
        do {
            buffer = new byte[DATAGRAM_MAX_SIZE];
            inBoundPacket = new DatagramPacket(buffer, buffer.length, ipAddress, socket.getLocalPort());
            socket.receive(inBoundPacket);

            byte code = buffer[1];
            if (code == OP_ERROR) {
                String errCode = new String(buffer, 3, 1);
                String errMsg = new String(buffer, 4, inBoundPacket.getLength() - 4);
                System.err.println("ERROR: " + errCode + errMsg);
            }
            else if (code == OP_DATA) {
                byte[] blockNum = {buffer[2], buffer[3]};
                DataOutputStream data = new DataOutputStream(ret);
                // start at the 4th byte and continue until the end of the packet
                data.write(inBoundPacket.getData(), 4, inBoundPacket.getLength() - 4);
                sendAck(blockNum);
            }
        } while (inBoundPacket.getLength() > 512);
        return ret;
    }

    private void writeToDisc(ByteArrayOutputStream data, String file) throws IOException {
        stream = new FileOutputStream(file);
        data.writeTo(stream);
    }

    private void sendFile() throws IOException {
        // TODO: Fix this to include a loop
        // We get to this method after we receive the first ack after our write request
        if(fis.available() == 0) {
            System.out.println("Closing...");
            fis.close();
            return;
        }

        byte[] fileBlockHeader = { 0, OP_DATA, (byte) (blockNum >> 8), (byte) (blockNum) };
        
        System.out.printf("Sending %s%n", Arrays.toString(fileBlockHeader));

        blockNum = (short) (blockNum + 1);

        if(fis.available() >= DATAGRAM_MAX_SIZE - 4) {
            buffer = new byte[DATAGRAM_MAX_SIZE - 4];
        } else {
            buffer = new byte[fis.available()];
        }

        System.out.printf("%d bytes left in file...%n", fis.available());

        fis.read(buffer);

        ByteArrayOutputStream fileBlockOS = new ByteArrayOutputStream();

        // messing with encoding
        // byte[] test = new String(buffer, StandardCharsets.US_ASCII).getBytes();

        // concat arrays
        fileBlockOS.write(fileBlockHeader);
        fileBlockOS.write(buffer);

        byte[] fileBlock = fileBlockOS.toByteArray();

        outBoundPacket = new DatagramPacket(fileBlock, fileBlock.length, ipAddress, socket.getLocalPort());

        socket.send(outBoundPacket);
    }

    public boolean receiveAck(byte[] expectedAck) throws IOException {
        // pass in the expected block number we should be receiving in our ack
        boolean receivedAck = false;
        buffer = new byte[DATAGRAM_MAX_SIZE];
        inBoundPacket = new DatagramPacket(buffer, buffer.length, ipAddress, socket.getLocalPort());
        socket.receive(inBoundPacket);

        byte code = buffer[1];
        if (code == OP_ERROR) {
            String errCode = new String(buffer, 3, 1);
            String errMsg = new String(buffer, 4, inBoundPacket.getLength() - 4);
            System.err.println("ERROR: " + errCode + errMsg);
        }
        else if (code == OP_ACK) {
            byte[] blockNum = {buffer[2], buffer[3]};
            System.out.println("received ack ");
            if (Arrays.equals(blockNum, expectedAck)) {
                receivedAck = true;
            }
        }
        return receivedAck;
    }

    public void closeSockets() {
        try {
            if(socket != null) socket.close();
            if(fis != null) fis.close();
            if(stream != null) stream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private void printByteArrayAsString(byte[] d) {
        InputStreamReader input = new InputStreamReader(
            new ByteArrayInputStream(d), Charset.forName("UTF-8"));

        StringBuilder str = new StringBuilder();
        try {
            for (int value; (value = input.read()) != -1;)
                str.append((char) value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(str.toString());
    }

    private void sendAck(byte[] num) throws IOException {
        byte[] ackByte = { 0, OP_ACK, num[0], num[1] };
        DatagramPacket ackPacket = new DatagramPacket(ackByte, ackByte.length, ipAddress, inBoundPacket.getPort());
        socket.send(ackPacket);
    }

    private byte[] formatRequestWritePacket(final byte OP_CODE, final String file, final String mode) {
        // 2 bytes for op code, file name, 0 byte, mode, 0 byte
        int size = 2 + file.length() + 1 + mode.length() + 1;
        byte[] ret = new byte[size];
        int pos = 0;
        
        ret[pos++] = zeroConst;
        ret[pos++] = OP_CODE;
        for (int i = 0; i < file.length(); i++) {
            ret[pos++] = (byte) file.charAt(i);
        }
        ret[pos++] = zeroConst;
        for (int i = 0; i < mode.length(); i++) {
            ret[pos++] = (byte) mode.charAt(i);
        }        
        ret[pos] = zeroConst;
        return ret;
    }
}