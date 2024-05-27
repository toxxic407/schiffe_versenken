import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SpielComputerNoUI {
	private BufferedReader in; // Verpackung des Socket-Eingabestroms.
	private Writer out; // Verpackung des Socket-Ausgabestroms.
	private boolean isPlayersTurn;
	private int[] lastPositionAttacked = new int[2];
	private int[] indexToAttackNext = new int[2];
	/*
	 * Werte für enemyField: 0 = unbekant, 1 = geschossen und Wasser gefunden, 2 =
	 * geschossen und Schief getroffen
	 */
	private int[][] enemyField;
	/*
	 * Werte für friendlyField: 0 = Wasser, 1 = Schiefteil intakt, 2 = Wasser
	 * Geschossen, 3 = Schiefteil getroffen
	 */
	private int[][] friendlyField;
	private int totalShipPartsCount;
	private int fieldSize = 0;
	private int anzahlSchiffeGroesse5 = 0;
	private int anzahlSchiffeGroesse4 = 0;
	private int anzahlSchiffeGroesse3 = 0;
	private int anzahlSchiffeGroesse2 = 0;
	private boolean isClientFieldSizeDone = false;
	private boolean areClientFieldShipsDone = false;
	private boolean isOpponentReady = false;

	public SpielComputerNoUI(int fieldSize, int anzahlSchiffeGroesse5, int anzahlSchiffeGroesse4,
			int anzahlSchiffeGroesse3, int anzahlSchiffeGroesse2) {
		this.fieldSize = fieldSize;
		friendlyField = new int[fieldSize][fieldSize];
		enemyField = new int[fieldSize][fieldSize];
		this.anzahlSchiffeGroesse5 = anzahlSchiffeGroesse5;
		this.anzahlSchiffeGroesse4 = anzahlSchiffeGroesse4;
		this.anzahlSchiffeGroesse3 = anzahlSchiffeGroesse3;
		this.anzahlSchiffeGroesse2 = anzahlSchiffeGroesse2;

	}

	private int countValueOcurrencesInArray(int[][] arrayToAnalyze, int valueToFind) {
		int output = 0;
		for (int i = 0; i < arrayToAnalyze.length; i++)
			for (int j = 0; j < arrayToAnalyze.length; j++) {
				if (valueToFind == arrayToAnalyze[i][j])
					output++;
			}

		return output;
	}

	private void attack() {

		try {

			int[] positionToAttackNow = new int[] { indexToAttackNext[0], indexToAttackNext[1] };
			lastPositionAttacked = indexToAttackNext; // save last attacked index
			

			// Strategy attack from right to left
			
			indexToAttackNext[1] = indexToAttackNext[1] + 1;
			
			// Prepare attack for next turn
			// Make sure that index of column is not out of index
			if (indexToAttackNext[1] >= fieldSize) {
				System.out.println("Restarting column index and going to next row");
				// going to next row
				indexToAttackNext[0] = indexToAttackNext[0] + 1;
				// restart column index
				indexToAttackNext[1] = 0;
			}

			// send attack
			System.out.println();
			out.write(String.format("shot %d %d%n", positionToAttackNow[0], positionToAttackNow[1]));
			out.flush();

		} catch (IOException ex) {
			System.out.println("write to socket failed");
		}

	}

	static int search(String[] arr, String s) {
		/* Returns count of occurrences of string s in arr[] */
		int counter = 0;
		for (int j = 0; j < arr.length; j++)

			if (s.equals(arr[j]))
				counter++;

		return counter;
	}

	
	private void placeAllShips() {
		placeShips(anzahlSchiffeGroesse5, 5);
		placeShips(anzahlSchiffeGroesse4, 4);
		placeShips(anzahlSchiffeGroesse3, 3);
		placeShips(anzahlSchiffeGroesse2, 2);
	}
	
	
	private void placeShips(int numberOfShips, int shipSize) {
		Random rand = new Random();
		int count = 0;
		// Loop until all ships of this size are placed
		while (count < numberOfShips) {
			boolean placed = false;
			// Try to place a ship until it is successfully placed
			while (!placed) {
				int row = rand.nextInt(fieldSize);
				int col = rand.nextInt(fieldSize);
				boolean horizontal = rand.nextBoolean();
				// Check if the ship can be placed at this position
				if (canPlaceShip(row, col, shipSize, horizontal)) {
					// Place the ship and update the placed flag and count
					placeShip(row, col, shipSize, horizontal, 1);
					placed = true;
					count++;
				}
			}
		}
	}

	private boolean canPlaceShip(int row, int col, int shipSize, boolean horizontal) {
		if (horizontal) {
			// Check if the ship fits horizontally
			if (col + shipSize > fieldSize)
				return false;
			// Check surrounding cells for the minimum distance rule (distance among ships:
			// at least 1)
			for (int i = -1; i <= shipSize; i++) {
				for (int j = -1; j <= 1; j++) {
					int newRow = row + j;
					int newCol = col + i;
					if (isInBounds(newRow, newCol) && this.friendlyField[newRow][newCol] != 0) {
						return false;
					}
				}
			}
		} else {
			// Check if the ship fits vertically
			if (row + shipSize > fieldSize)
				return false;
			// Check surrounding cells for the minimum distance rule
			for (int i = -1; i <= shipSize; i++) {
				for (int j = -1; j <= 1; j++) {
					int newRow = row + i;
					int newCol = col + j;
					if (isInBounds(newRow, newCol) && this.friendlyField[newRow][newCol] != 0) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void placeShip(int row, int col, int shipSize, boolean horizontal, int shipValue) {
		if (horizontal) {
			// Place the ship horizontally
			for (int i = 0; i < shipSize; i++) {
				friendlyField[row][col + i] = shipValue;
			}
		} else {
			// Place the ship vertically
			for (int i = 0; i < shipSize; i++) {
				friendlyField[row + i][col] = shipValue;
			}
		}
	}

	private boolean isInBounds(int row, int col) {
		/**
		 * Method to check if a position is within the bounds of the field
		 */
		return row >= 0 && row < fieldSize && col >= 0 && col < fieldSize;
	}

	
	public void start() throws IOException {

		indexToAttackNext = new int[] { 0, 0 };

		// role is always Client when the player is Computer with no UI. Server is
		// localhost
		isPlayersTurn = false; // Server starts playing

		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		Socket s = new Socket("localhost", port);

		System.out.println("Connection established.");

		// register total number of ship parts
		totalShipPartsCount = countValueOcurrencesInArray(friendlyField, 1);

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new OutputStreamWriter(s.getOutputStream());

		
		// get field size
		while (!isClientFieldSizeDone) {
			System.out.println("Waiting for field size...");
			String line = in.readLine(); // read line from socket
			System.out.println("received: " + line);
			String[] responseList = line.split(" "); // split line based on whitespace

			if (responseList[0].equals("size")) {
				this.fieldSize = Integer.parseInt(responseList[1]);
				
				isClientFieldSizeDone = true;

				// answer "done"
				out.write(String.format("done %n"));
				out.flush();
				
				System.out.println("Size recieved correctly, send done response...");

			}
		}
		
		// TODO create field according to obtained data
		this.friendlyField = new int[this.fieldSize][this.fieldSize];
		this.enemyField = new int[this.fieldSize][this.fieldSize];

		// get number of each ship size
		while (!areClientFieldShipsDone) {
			String line = in.readLine(); // read line from socket
			System.out.println("received: " + line);
			String[] responseList = line.split(" "); // split line based on whitespace

			if (responseList[0].equals("ships")) {
				// get number of ship of each size
				this.anzahlSchiffeGroesse5 = search(responseList, "5");
				this.anzahlSchiffeGroesse4 = search(responseList, "4");
				this.anzahlSchiffeGroesse3 = search(responseList, "3");
				this.anzahlSchiffeGroesse2 = search(responseList, "2");
				
				areClientFieldShipsDone = true;

				// answer "done"
				out.write(String.format("done %n"));
				out.flush();
			}
		}
		
		// place ships in random positions
		placeAllShips();

		// wait until server is ready
		while (!isOpponentReady) {
			String line = in.readLine(); // read line from socket
			System.out.println("received: " + line);
			String[] responseList = line.split(" "); // split line based on whitespace
			
			if (responseList[0].equals("ready")) {
				isOpponentReady = true;

				// answer "ready"
				out.write(String.format("ready %n"));
				out.flush();
			}
			
		}

		
		/*	
		// since role is Client, send first attack if did not send yet
		if (iAmClientAndSentFirstAttackAlready == false) {

			System.out.println("I am the client. I should attack first.");

			attack();

			iAmClientAndSentFirstAttackAlready = true;

			System.out.println("First attack sent");

		}
		*/
		
		

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
				int positionAttackedRow = Integer.parseInt(responseList[1]);
				int positionAttackedColumn = Integer.parseInt(responseList[2]);
				int attackResult = -1;
				// TODO attackResult = 2

				// prepare answer according to the content of the attacked position
				switch (friendlyField[positionAttackedRow][positionAttackedColumn]) {
				case 0: // es gibt Wasser in indexAttacked - 1
				{
					attackResult = 0;
					isPlayersTurn = true;
					// System.out.println(role+"'s turn now");
					break;
				}
				case 1: // es gibt Schieffteil in indexAttacked - 1
				{
					// TODO see if the ship has been destroyed completely to send 2
					attackResult = 1;
					friendlyField[positionAttackedRow][positionAttackedColumn] = 3; // change to hit ship part
					isPlayersTurn = false;
					// System.out.println("NOT " + role + "'s turn now");
					
					 
					break;
				}

				}

				// send answer
				//System.out.println("sent: " + String.format("answer %d%n", attackResult));
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
					enemyField[lastPositionAttacked[0]][lastPositionAttacked[1]] = 1;
					isPlayersTurn = false;
					// System.out.println("NOT " + role + "'s turn anymore");
					// send answer "pass"
					out.write(String.format("pass %n"));
					out.flush();
					break;
				}
				case 1: // geschossen und Schief getroffen
				{
					enemyField[lastPositionAttacked[0]][lastPositionAttacked[1]] = 2;
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
	
	public static void main(String[] args) throws IOException {
		SpielComputerNoUI spieler = new SpielComputerNoUI(10, 1, 3,
				2, 1);
		
		spieler.start();
	}

}
