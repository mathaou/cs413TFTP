package app;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;

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
    // TODO: Fix these
    private final int TIMEOUT = 2000; // 2 seconds
    private final int TOTAL_RETRIES = 5;

    private short blockNum, expectedBlockNum;

    public TFTPClient(final String server_ip) throws UnknownHostException, SocketException {
        SERVER_IP = server_ip;
        ipAddress = InetAddress.getByName(SERVER_IP);
        socket = new DatagramSocket();
    }

    // TODO: read up on the stuff pieces we found to understand what Dr. C was talking about
    // why do we need to handle cardinal integers?

    public String getDataType() {
        System.out.println("\n\t1. Octet\n\t2. Netascii");
        System.out.print("Choose data type: ");
        String response = Main.scan.nextLine();

        switch(response){
            case "1":
                return MODE_OCTET;
            case "2":
                return MODE_NETASCII;
            default:
                System.out.println("Not a valid selection.");
                return null;
        }
    }

    public void getFile(final String file) throws IOException {
    	socket.setSoTimeout(TIMEOUT);
        System.out.println("Beginning request to get " + file + " from server.");
        // create read request and send packet
        request = formatRequestWritePacket(OP_RRQ, file, getDataType());
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        socket.send(outBoundPacket);
        // receive file and write to disc
        ByteArrayOutputStream inData = receiveFile(outBoundPacket); //Sends the header packet
        if(inData == null) return; //Will return null if timed out 5 times
        writeToDisc(inData, file);
        inData.close(); 
        System.out.println("Terminating connection with server.");
    }

    public void putFile(final String file) throws IOException {
        blockNum = (short) 1;
        expectedBlockNum = (short) 0;
        // load file
        fis = new FileInputStream(file);
        System.out.println("Beginning request to write " + file + " to server.");
        // create write request and send packet
        request = formatRequestWritePacket(OP_WRQ, file, getDataType());
        outBoundPacket = new DatagramPacket(request, request.length, ipAddress, DEFAULT_SERVER_PORT);
        
        buffer = new byte[DATAGRAM_MAX_SIZE];
        inBoundPacket = new DatagramPacket(buffer, buffer.length, ipAddress, socket.getLocalPort());
        
        socket.send(outBoundPacket);

        byte[] startReceive = {(byte) (expectedBlockNum >> 8), (byte) (expectedBlockNum)};
        boolean received = true;
        int tries = 5;
        do {
            try {
                received = receiveAck(startReceive);
            } catch (SocketTimeoutException e) {
                socket.send(outBoundPacket);
                tries--;
            }
        } while (!received || tries > 0);

        if(tries == 0) {
            System.err.println("Reached maximum retry count. General failure.");
            return;
        }
        sendFile(inBoundPacket.getPort());
        System.out.println("Terminating connection with server.");
    }

    private ByteArrayOutputStream receiveFile(DatagramPacket outBoundPacket) throws IOException {
        ret = new ByteArrayOutputStream();
        do {
            buffer = new byte[DATAGRAM_MAX_SIZE];
            inBoundPacket = new DatagramPacket(buffer, buffer.length, ipAddress, socket.getLocalPort());
            
            //Start timeout checking
            int tries = TOTAL_RETRIES;
            boolean ack = false;
            do {
            	try {
                	socket.receive(inBoundPacket);//Gets packet
                	ack = true;
                }
                catch(IOException e) {
                	System.out.println("Attempted to resend packet...");//Timed out
                	socket.send(outBoundPacket);
                	tries--;
                }
            } while(tries > 0 && !ack);

            if(tries == 0) {
            	System.err.println("Reached maximum retry count. General failure.");
            	return null;
            }
            
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
                
                System.out.printf("Sending ack for packet %s...%n", Arrays.toString(blockNum));
                sendAck(blockNum);
            }
        } while (inBoundPacket.getLength() > 512);
        return ret;
    }

    private void writeToDisc(ByteArrayOutputStream data, String file) throws IOException {
        stream = new FileOutputStream(file);
        data.writeTo(stream);
    }

    private void sendFile(int serverPort) throws IOException {
        boolean ackReceived;
        int retries;
        socket.setSoTimeout(TIMEOUT);

        do {
            ackReceived = false;
            byte[] fileBlockHeader = { 0, OP_DATA, (byte) (blockNum >> 8), (byte) (blockNum) };
        
            System.out.printf("Sending %s%n", Arrays.toString(fileBlockHeader));

            if(fis.available() >= DATAGRAM_MAX_SIZE - 4) {
                buffer = new byte[DATAGRAM_MAX_SIZE - 4];
            } else {
                buffer = new byte[fis.available()];
            }

            System.out.printf("%d bytes left in file...%n", fis.available());

            fis.read(buffer);

            ByteArrayOutputStream fileBlockOS = new ByteArrayOutputStream();

            // concat arrays
            fileBlockOS.write(fileBlockHeader); // header
            fileBlockOS.write(buffer); // data

            byte[] fileBlock = fileBlockOS.toByteArray();

            outBoundPacket = new DatagramPacket(fileBlock, fileBlock.length, ipAddress, serverPort);
            
            buffer = new byte[DATAGRAM_MAX_SIZE];
            inBoundPacket = new DatagramPacket(buffer, buffer.length, ipAddress, socket.getLocalPort());

            // send the packet once and instantiate the retry count
            socket.send(outBoundPacket);
            retries = TOTAL_RETRIES;
            while(!ackReceived || retries == 0) {
                try {
                    ackReceived = receiveAck(new byte[] { (byte) (expectedBlockNum >> 8), (byte) (expectedBlockNum) });
                } catch (SocketTimeoutException e) {
                    socket.send(outBoundPacket);
                    continue;
                }
                if (checkAck(buffer, blockNum) && fis.available() >= DATAGRAM_MAX_SIZE - 4) {
                    System.out.println("Received ack: " + blockNum);
                    expectedBlockNum = (short) (expectedBlockNum + 1);
                    break;
                } else if (fis.available() < DATAGRAM_MAX_SIZE - 4) { // less than 512 signals end of transmission
                    ackReceived = true; // 
                } else {
                    retries--;
                }
            }
            if (retries == 0) {
                System.err.println("Reached maximum retry count. General failure.");
                return;
            }
            blockNum = (short) (blockNum + 1);
        } while (fis.available() > 0);

        System.out.println("Closing...");
    }

    public boolean receiveAck(byte[] expectedAck) throws IOException {
        // pass in the expected block number we should be receiving in our ack
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
            if (Arrays.equals(blockNum, expectedAck)) {
                return true;
            }
        }
        return false;
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
    
    private boolean checkAck(byte[] buffer, Short blockNumber) { //This is just used for testing to check increment of block num
    	byte code = buffer[1];
    	return code == OP_ACK;
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