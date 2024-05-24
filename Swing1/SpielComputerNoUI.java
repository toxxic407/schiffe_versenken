import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SpielComputerNoUI {
	private String role; // Rolle: Server oder Client.
	private BufferedReader in; // Verpackung des Socket-Eingabestroms.
	private Writer out; // Verpackung des Socket-Ausgabestroms.
	private boolean isPlayersTurn;
	private int lastPositionAttacked;
	private int indexToAttackNext;
	/*
	 * Werte für enemyField: 0 = unbekant, 1 = geschossen und Wasser gefunden, 2 =
	 * geschossen und Schief getroffen
	 */
	private int[] enemyField = new int[64];
	/*
	 * Werte für friendlyField: 0 = Wasser, 1 = Schiefteil intakt, 2 = Wasser
	 * Geschossen, 3 = Schiefteil getroffen
	 */
	private int[] friendlyField = new int[64];
	private int totalShipPartsCount;
	private boolean iAmClientAndSentFirstAttackAlready; // used to make the Client send the first attack
	private String ipServer;

	public SpielComputerNoUI(String ipServer) {
		this.ipServer = ipServer;
	}

	private int countValueOcurrencesInArray(int[] arrayToAnalyze, int valueToFind) {
		int output = 0;
		for (int i = 0; i < arrayToAnalyze.length; i++)
			if (valueToFind == arrayToAnalyze[i])
				output++;
		return output;
	}

	private void attack() {

		try {

			int positionToAttackNow = indexToAttackNext;
			lastPositionAttacked = indexToAttackNext; // save last attacked index
			indexToAttackNext++;

			// send attack
			out.write(String.format("shot %d%n", positionToAttackNow + 1));
			out.flush();

		} catch (IOException ex) {
			System.out.println("write to socket failed");
		}

	}

	public void start() throws IOException {

		indexToAttackNext = 0;

		// make friendly field have at least one ship
		// if random number is even, the field it has one structure, if not, another
		// structure
		Random rand = new Random();

		int n = rand.nextInt(10);
		// add Schieff of lenght 3
		if (n % 2 == 0) {
			friendlyField[10] = 1;
			friendlyField[11] = 1;
			friendlyField[12] = 1;
		} else {
			friendlyField[38] = 1;
			friendlyField[46] = 1;
			friendlyField[54] = 1;
		}

		// add Schief of lenght 2
		int n2 = rand.nextInt(10);
		// add Schieff of lenght 3
		if (n2 % 2 == 0) {
			friendlyField[50] = 1;
			friendlyField[51] = 1;
		} else {
			friendlyField[26] = 1;
			friendlyField[34] = 1;
		}

		// add Schief of lenght 1
		int n3 = rand.nextInt(10);
		if (n3 % 2 == 0) {
			friendlyField[0] = 1;

		} else {
			friendlyField[16] = 1;

		}

		// register total number of ship parts
		totalShipPartsCount = countValueOcurrencesInArray(friendlyField, 1);

		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		// Socketverbindung zur anderen "Seite" herstellen.
		Socket s;
		if (this.ipServer.length() == 0) {
			role = "Server";

			isPlayersTurn = false; // Server does not start playing

			// Die eigene(n) IP-Adresse(n) ausgeben,
			// damit der Benutzer sie dem Benutzer des Clients mitteilen kann.
			System.out.print("My IP address(es):");
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					InetAddress ia = ias.nextElement();
					if (!ia.isLoopbackAddress()) {
						System.out.print(" " + ia.getHostAddress());
					}
				}
			}
			System.out.println();
			System.out.println("Waiting for client connection ...");

			ServerSocket ss = new ServerSocket(port);
			s = ss.accept();
		} else {
			role = "Client";
			isPlayersTurn = true; // Client starts playing

			s = new Socket(this.ipServer, port);

			System.out.println();
			System.out.println();
		}
		System.out.println("Connection established.");

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new OutputStreamWriter(s.getOutputStream());

		// if player is Client and did not send first attack, send first attack
		if (role == "Client" && iAmClientAndSentFirstAttackAlready == false) {

			System.out.println("I am the client. I should attack first.");

			attack();

			iAmClientAndSentFirstAttackAlready = true;

			System.out.println("First attack sent");

		}

		// Netzwerknachrichten lesen und verarbeiten.
		// Da die graphische Oberfläche von einem separaten Thread verwaltet
		// wird, kann man hier unabhängig davon auf Nachrichten warten.
		// Manipulationen an der Oberfläche sollten aber mittels invokeLater
		// (oder invokeAndWait) ausgeführt werden.
		while (true) {
			String line = in.readLine(); // read line from socket
			System.out.println("received: " + line);
			String[] responseList = line.split(" "); // split line based on whitespace

			// get attack and answer it

			// player is receiving an attack
			if (responseList[0].equals("shot")) {
				// process attack
				int positionAttacked = Integer.parseInt(responseList[1]);
				int attackResult = -1;
				// TODO attackResult = 2

				// prepare answer according to the content of the attacked position
				switch (friendlyField[positionAttacked - 1]) {
				case 0: // es gibt Wasser in indexAttacked - 1
				{
					attackResult = 0;
					isPlayersTurn = true;
					// System.out.println(role+"'s turn now");
					break;
				}
				case 1: // es gibt Schieffteil in indexAttacked - 1
				{
					attackResult = 1;
					friendlyField[positionAttacked - 1] = 3; // change to hit ship part
					isPlayersTurn = false;
					// System.out.println("NOT " + role + "'s turn now");
					break;
				}

				}

				// send answer
				out.write(String.format("answer %d%n", attackResult));
				out.flush();


				// check if player lost: count of ships (value = 1) is 0
				if (countValueOcurrencesInArray(friendlyField, 1) == 0) {
					// System.out.println(role + " lost.");
					isPlayersTurn = false;

				} else if (isPlayersTurn) // if player did not lose and is its turn, attack
				{
					attack();
				}

			}

			else if (responseList[0].equals("answer")) // get attack answer and process it
			{

				int attackAnswerNumber = Integer.parseInt(responseList[1]);

				// TODO for case 2 (Schieff gesunken)
				switch (attackAnswerNumber) {
				case 0: // Wasser geschossen
				{
					enemyField[lastPositionAttacked] = 1;
					isPlayersTurn = false;
					// System.out.println("NOT " + role + "'s turn anymore");
					// send answer "pass"
					out.write(String.format("pass %n"));
					out.flush();
					break;
				}
				case 1: // geschossen und Schief getroffen
				{
					enemyField[lastPositionAttacked] = 2;
					isPlayersTurn = true;
					// System.out.println("Still " +role+"'s turn");
					break;
				}

				}

				// check if player won: count of hit ships (value = 2) is totalShipPartsCount
				if (countValueOcurrencesInArray(enemyField, 2) == totalShipPartsCount) {
					// System.out.println(role + " won.");
					isPlayersTurn = false;

				} else if (isPlayersTurn) // if player did not win and is its turn, continue attack
				{
					attack();
				}

			}

			if (line == null)
				break;
			/*
			 * SwingUtilities.invokeLater( () -> { button.setEnabled(true); } );
			 */
		}

		// EOF ins Socket "schreiben" und das Programm explizit beenden
		// (weil es sonst weiterlaufen würde, bis der Benutzer das Hauptfenster
		// schließt).
		s.shutdownOutput();
		System.out.println("Connection closed.");
		System.exit(0);

	}

}
