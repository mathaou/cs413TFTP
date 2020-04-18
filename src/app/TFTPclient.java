package app;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TFTPclient {

    // Server configurations
    private static String SERVER_IP = "";
    private static final int DEFAULT_SERVER_PORT = 69;

    // OP codes
    private static final byte OP_RRQ = 1;
    private static final byte OP_WRQ = 2;
    private static final byte OP_DATA = 3;
    private static final byte OP_ACK = 4;
    private static final byte OP_ERROR = 5;

    // Data modes
    private static final String MODE_NETASCII = "netascii";
    private static final String MODE_OCTET = "octet";
    private static final String MODE_MAIL = "mail";

    // Networking inforamtion
    private InetAddress ipAddress = null;
    private DatagramSocket socket = null;
    private DatagramPacket outBoundPacket;
    private DatagramPacket inBoundPacket;

    // other
    private final static int DATAGRAM_MAX_SIZE = 516;
    private byte[] request;
    private byte[] buffer;
    private byte zeroConst = 0;

    public TFTPclient(final String server_ip) throws UnknownHostException, SocketException {
        SERVER_IP = server_ip;
        ipAddress = InetAddress.getByName(SERVER_IP);
        socket = new DatagramSocket();
    }

    public void getFile(final String file) throws IOException {
        System.out.println("Beginning request to get " + file + " from server.");
        // create read request and send packet
        request = formatRequestWritePacket(OP_RRQ, file, MODE_NETASCII);
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        socket.send(outBoundPacket);
        // receive file and write to disc
        ByteArrayOutputStream inData = receive();
        writeToDisc(inData, file);
    }

    public void putFile(final String file) throws IOException {
        System.out.println("Beginning request to write " + file + " to server.");
        // create write request and send packet
        request = formatRequestWritePacket(OP_WRQ, file, MODE_NETASCII);
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        socket.send(outBoundPacket);
        // send file to server
        send(file);
    }

    private ByteArrayOutputStream receive() throws IOException {
        ByteArrayOutputStream ret = new ByteArrayOutputStream();
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
                byte[] blockNum = {buffer[2], buffer[3] };
                DataOutputStream data = new DataOutputStream(ret);
                // start at the 4th byte and continue until the end of the packet
                data.write(inBoundPacket.getData(), 4, inBoundPacket.getLength() - 4);
                sendAck(blockNum);
            }


        } while (inBoundPacket.getLength() >= 512);
        return ret;
    }

    private void writeToDisc(ByteArrayOutputStream data, String file) throws IOException {
        OutputStream stream = new FileOutputStream(file);
        data.writeTo(stream);
    }

    private void send(String file) {
        // TODO: Finish the put file
    }

    private void sendAck(byte[] num) throws IOException {
        byte[] ackByte = { 0, OP_ACK, num[1], num[2] };
        DatagramPacket ackPacket = new DatagramPacket(ackByte, ackByte.length, ipAddress, inBoundPacket.getPort());
        socket.send(ackPacket);
    }

    private byte[] formatRequestWritePacket(final byte OP_CODE, final String file, final String mode) {
        // 2 bytes for op code, file name, 0 byte, mode, 0 byte
        int size = 2 + file.length() + 1 + mode.length() + 1;
        byte[] ret = new byte[size];
        int pos = 0;
        
        ret[pos] = zeroConst;
        pos++;
        ret[pos] = OP_CODE;
        pos++;
        for (int i = 0; i < file.length(); i++) {
            ret[pos] = (byte) file.charAt(i);
            pos++;
        }
        ret[pos] = zeroConst;
        pos++;
        for (int i = 0; i < mode.length(); i++) {
            ret[pos] = (byte) mode.charAt(i);
            pos++;
        }        
        ret[pos] = zeroConst;
        return ret;
    }
}