import java.io.IOException;
import java.util.Scanner;

import app.TFTPClient;

public class Main {

	private final String cichanowski =
    "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdolcccclox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMMMWKkdc;'...........;okXMMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMNOoc::;,'''''',,,;;,'..;xXMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMNk:,:lc::;,,''''',:loddoc,.;xNMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMNd,,oO0Oxolccccc::ccodxkkxl,.'c0WMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMWO;,d0000KK00OkxdddxxkOkkkkdl,''cKMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMNd;lk000000KKKKK00000OOkkkkxdc',:kWMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMXo:odk00KKKKKK00000000Okkxxxo:'';xNMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMKdooxO0OOOkkxkOOOOOOOOkkkkxdc,..,dXMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMW0dllxkxoc:;,';okOkoc::::clool,.';oXMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMNklc:ldxxoc;,,;cok0x:''...';:cl;',:xNMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMNo:ookkkOOkdoodllOKx;;cc::ccllc,',;cOMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMM0dloO0OOkxxxolokOOxc:loddxddoo:;;,'xWMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMW0dxOOOOxddoldOOOkxoc:clodddddl,';oXMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMWKOOOkOOkxxkdllllc::cloolllloo:,oXMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMWXKOkkxdxO0OkdollccloollclodlcxNMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMW0OOxdolllcllcc:::cccllldkk0WMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMWXK0Oxoddolcc:::;;;;coddxKMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMMWX0OO00Okkxdooollc;codxOWMMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMMWOdkO0KK000OkkkxdxdlcloocxNMMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMMMMMW0:,dxxk000Okxxxxxxdlcccc'.dNMMMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMMMMMMWNX0kk:,ldllloolccclllc:;::::. ;dONMMMMMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMMMMMMWNXKOxlolokd;:lcc:::,,,,;;;;;;;,,,..:,.ckOKNMMMMMMMMMMMMMMMMM "+
    "\nMMMMMMMMMWNXK0Okxxxdloooxxc:c:::::;;;;;,,,,,,,,..,c'..:cloxOKNWMMMMMMMMMMMM "+
    "\nMMMMMWNKOxdoodolcoolcll;lddlclccc;,,,,,',,,,,''',:;...,ccc:;;cx0XWMMMMMMMMM "+
    "\nMMWKOdloocllc:,':oodddo:,lo;,oOkdc:;,,,;;;;,'.;;,,.....,:::,',;coxOXWMMMMMM "+
    "\nNK0xlccoxodxoc;;dxxkkOdc;;,..:d0KOxdooolc:;'..;:,''..',;:;;'...,;:lldOXWMMM "+
    "\nOOOkollododkxoclxxxxkkxc::;'..';dO0KXKK0o,'. .:;,::'';cllc:,.';ccodoc;:xNMM "+
    "\nokkxl:;clloxxl:cododdxo:,,;;.',,',;o0NX0l;;'...':l:',cooool:;;codxxxo:,;d0W "+
    "\nodolc;',;;coo:',:cccloo:,,:llodolc,'cxd:',;c:'.;cc,.,looooc:,;odxxxxo:;:llk "+
    "\ndxolloccccclol;:looddxxocclxOOOOkko:;,..,;cooolcl;..':cccc;'';lcloll:,,:ccl "+
    "\nldxxxxoloodkkdccoxkkkkkoccokOOOkkkdl:;:odoxkkkxddc,':llloo;..,c::cl:'..:ol: "+
    "\n:oxxxxlododkxoccoxkkkOOdllokOOOkxxocldkOxdxxkkxxxl;;lxdddxo;'coooodl,';oxd: "+
    "\n,;coddlodoxkxlccoxkOOOko::lxxxkxddoloxkkxxxkkkxxxl;;oddddxd:,ldoodo:,;ldxc. "+
    "\n;,;:clccclxkd:,,coxxxxdc,,;loodollc:ldxdoodxkkxxxl,;oddddxd:,ldlodo;':odl,. ";

	private Scanner scan = null;
	private TFTPClient client = null;
	private String input = null;

	public static void main(String[] args) throws IOException {
		new Main();
	}

	public Main(){
        scan = new Scanner(System.in);
        boolean end = false;
		System.out.printf("%s%n", cichanowski);

		while(!end) {
			System.out.println("TFTP Client");
			System.out.println("Options:");
			if (client != null) {
				System.out.println("\t0 - Change server address");
			}
			System.out.println("\t1 - Read Request");
			System.out.println("\t2 - Write Request");
			System.out.println("\t3 - Exit");
			String option = scan.nextLine();
			try {
				switch(option) {
					case "1":
						if (client == null) handleServerLogin(scan);
						System.out.print("\nEnter File Name Requested: ");
						input = scan.nextLine();
						client.getFile(input); 
					break;
					case "2":
						if (client == null) handleServerLogin(scan);
						System.out.print("\nEnter File Name Requested: ");
						input = scan.nextLine();
						client.putFile(input);
					break;
					case "3":
						end = true;
						client.closeSockets();
					break;
					default:
						if(option.equals("0") && client != null){
							handleServerLogin(scan);
						} else {
							System.out.println("Command not recognized.");
						}
				}
			} catch (Exception e){ 
				e.printStackTrace(); 
			}
		}

		System.out.println("Exiting program.");
        scan.close();
	}

	public void handleServerLogin(Scanner scan) {
		if (client != null) client.closeSockets();
		System.out.print("Enter server address: ");
		input = scan.nextLine();
		System.out.printf("Attempting to connect to %s...%n", input);
		try {
			client = new TFTPClient(input);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
