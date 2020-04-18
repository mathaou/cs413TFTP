import java.io.IOException;
import java.util.Scanner;

import app.TFTPClientNet;

public class main {

    public static void main(String[] args) throws IOException {
		
		TFTPClientNet tftpClient = new TFTPClientNet();
        Scanner scan = new Scanner(System.in);
        boolean end = false;

		while(!end) {
			System.out.println("TFTP Client");
			System.out.println("Options:");
			System.out.println("1 - Read Request");
			System.out.println("2 - Write Request");
			System.out.println("3 - Exit");
			String option = scan.nextLine();
			String fileName = null;
			switch(option) {
				case "1":
					System.out.print("Enter File Name Requested: ");
					fileName = scan.nextLine();
					tftpClient.get(fileName);
				break;
				case "2":
					System.out.print("Enter File Name Requested: ");
					fileName = scan.nextLine();
					tftpClient.put(fileName);
				break;
				case "3":
					end = true;
					System.out.println("TFTP Client Exiting...");
				break;
				default:
					System.out.println("Command Not Recognized");
			}
        }
        
        scan.close();
	}

}
