import java.io.IOException;
import java.util.Scanner;

import app.TFTPclient;

public class main {

    public static void main(String[] args) throws IOException {
		
		TFTPclient client;
        Scanner scan = new Scanner(System.in);
        boolean end = false;

		while(!end) {
			System.out.println("TFTP Client");
			System.out.println("Options:");
			System.out.println("1 - Read Request");
			System.out.println("2 - Write Request");
			System.out.println("3 - Exit");
			String option = scan.nextLine();
			String input = null;
			switch(option) {
				case "1":
					System.out.print("Enter server address: ");
					input = scan.nextLine();
					System.out.println("Connecting to " + input);
					client = new TFTPclient(input);
					System.out.print("Enter File Name Requested: ");
					input = scan.nextLine();
					client.getFile(input);
					System.out.println("Terminating connection with server.");
				break;
				case "2":
					System.out.print("Enter server address: ");
					input = scan.nextLine();
					System.out.println("Connecting to " + input);
					client = new TFTPclient(input);
					System.out.print("Enter File Name Requested: ");
					input = scan.nextLine();
					client.putFile(input);
					System.out.println("Terminating connection with server.");
				break;
				case "3":
					end = true;
				break;
				default:
					System.out.println("Command not recognized");
			}
		}

		System.out.println("Exiting program.");
        scan.close();
	}

}
