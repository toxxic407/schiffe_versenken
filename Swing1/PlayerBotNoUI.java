import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

public class PlayerBotNoUI {
	private BufferedReader in; // Verpackung des Socket-Eingabestroms.
	private Writer out; // Verpackung des Socket-Ausgabestroms.
	private boolean isPlayersTurn;
	private int[] lastPositionAttacked = new int[2];
	private int[] indexToAttackNext = new int[2];
	/*
	 * Werte für enemyField: 0 = unbekant, 1 = geschossen und Wasser gefunden, 2 =
	 * geschossen und Schief getroffen, 3 = Schiff destroyed (Treffer-versenkt)
	 */
	private int[][] enemyField;
	/*
	 * Werte für friendlyField: 0 = Wasser, 1 = Schiefteil intakt, 2 = Wasser
	 * Geschossen, 3 = Schiefteil getroffen, 4 = Schiff destroyed
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
	private Socket s;

	public PlayerBotNoUI(int fieldSize, int anzahlSchiffeGroesse5, int anzahlSchiffeGroesse4, int anzahlSchiffeGroesse3,
			int anzahlSchiffeGroesse2) {
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
			lastPositionAttacked = indexToAttackNext.clone(); // save last attacked index

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
		this.friendlyField = new int[this.fieldSize][this.fieldSize];
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

	private int[] getShipInfo(int x, int y) {
		int size = 0;
		boolean vertical = false;

		// Determine if the ship is vertical or horizontal
		if (x > 0 && (friendlyField[x - 1][y] == 1 || friendlyField[x - 1][y] == 3)
				|| x < fieldSize - 1 && (friendlyField[x + 1][y] == 1 || friendlyField[x + 1][y] == 3)) {
			vertical = true;
		}

		// Find the start of the ship
		int startX = x;
		int startY = y;
		if (vertical) {
			while (startX > 0 && (friendlyField[startX - 1][y] == 1 || friendlyField[startX - 1][y] == 3)) {
				startX--;
			}
			// Calculate the size of the ship
			while (startX + size < fieldSize
					&& (friendlyField[startX + size][y] == 1 || friendlyField[startX + size][y] == 3)) {
				size++;
			}
		} else {
			while (startY > 0 && (friendlyField[x][startY - 1] == 1 || friendlyField[x][startY - 1] == 3)) {
				startY--;
			}
			// Calculate the size of the ship
			while (startY + size < fieldSize
					&& (friendlyField[x][startY + size] == 1 || friendlyField[x][startY + size] == 3)) {
				size++;
			}
		}

		return new int[] { size, vertical ? 1 : 0, startX, startY };
	}

	private boolean isShipCompletelyDestroyed(int x, int y) {
		int countHitParts = 0;

		int[] shipInfo = getShipInfo(x, y);
		int shipSize = shipInfo[0];
		boolean vertical = shipInfo[1] == 1;
		int startShipX = shipInfo[2];
		int startShipY = shipInfo[3];

		if (vertical) {
			for (int i = 0; i < shipSize; i++) {
				if (friendlyField[startShipX + i][startShipY] == 3) {
					// ship part value = 3 means hit by enemy
					System.out.print("(" + (startShipX + i) + "," + startShipY + ")");
					countHitParts++;
				}

			}
		} else {

			for (int i = 0; i < shipSize; i++) {
				if (friendlyField[startShipX][startShipY + i] == 3) {
					// ship part value = 3 means hit by enemy
					System.out.print("(" + startShipX + "," + (startShipY + i) + ")");
					countHitParts++;
				}

			}
		}

		System.out.println();
		System.out.println("Hit parts: " + countHitParts + ", shipSize: " + shipSize);
		return countHitParts == shipSize;

	}

	private void manageSocketConnection() throws IOException {
		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		boolean isConnectionSuccesfull = false;

		while (!isConnectionSuccesfull) {
			try {
				Socket s = new Socket("localhost", port);
				this.s = s;
				isConnectionSuccesfull = true;
			} catch (Exception e) {
				continue;
			}

		}

		System.out.println("Connection established.");

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.out = new OutputStreamWriter(s.getOutputStream());
	}

	private void managePreparationBeforeBattle() throws IOException {
		// Preparation before battle
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

		// register total number of ship parts
		this.totalShipPartsCount = countValueOcurrencesInArray(friendlyField, 1);

		System.out.println(
				"countValueOcurrencesInArray(enemyField, 1) = " + countValueOcurrencesInArray(friendlyField, 1));

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
	}

	private void markDestroyedShip(int x, int y) {
		// get basic information about the ship
		int[] shipInfo = getShipInfo(x, y);
		int shipSize = shipInfo[0];
		boolean vertical = shipInfo[1] == 1;
		int startShipX = shipInfo[2];
		int startShipY = shipInfo[3];

		// change the values of the ship to mark it as destroyed, according to its
		// orientation
		if (vertical) {
			for (int i = 0; i < shipSize; i++) {
				friendlyField[startShipX + i][startShipY] = 4;

			}
		} else {

			for (int i = 0; i < shipSize; i++) {
				friendlyField[startShipX][startShipY + i] = 4;

			}
		}
	}

	private int[] getEnemyShipInfo(int x, int y) {
		int size = 0;
		boolean vertical = false;

		// Determine if the ship is vertical or horizontal
		if (x > 0 && (enemyField[x - 1][y] == 2) || x < fieldSize - 1 && (enemyField[x + 1][y] == 2)) {
			vertical = true;
		}

		// Find the start of the ship
		int startX = x;
		int startY = y;
		if (vertical) {
			while (startX > 0 && (enemyField[startX - 1][y] == 2)) {
				startX--;
			}
			// Calculate the size of the ship
			while (startX + size < fieldSize && (enemyField[startX + size][y] == 2)) {
				size++;
			}
		} else {
			while (startY > 0 && (enemyField[x][startY - 1] == 2)) {
				startY--;
			}
			// Calculate the size of the ship
			while (startY + size < fieldSize && (enemyField[x][startY + size] == 2)) {
				size++;
			}
		}

		return new int[] { size, vertical ? 1 : 0, startX, startY };

	}

	private void markDestroyedEnemyShip(int x, int y) {
		// get basic information about the ship
		int[] enemyShipInfo = getEnemyShipInfo(x, y);
		int shipSize = enemyShipInfo[0];
		boolean vertical = enemyShipInfo[1] == 1;
		int startShipX = enemyShipInfo[2];
		int startShipY = enemyShipInfo[3];

		// change the values of the ship to mark it as destroyed, according to its
		// orientation
		if (vertical) {
			for (int i = 0; i < shipSize; i++) {
				enemyField[startShipX + i][startShipY] = 3;

			}
		} else {

			for (int i = 0; i < shipSize; i++) {
				enemyField[startShipX][startShipY + i] = 3;

			}
		}
	}

	private boolean saveGameClient(String gameId) {
		// create folder to save game if it does not exist
		String directoryPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + "/savedGamesAsClient/";
		// Decode URL encoding
		directoryPath = URLDecoder.decode(directoryPath, StandardCharsets.UTF_8);

		// Ensure the directory exists
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			System.out.println("Directory does not exist, creating it.");
			boolean dirCreated = directory.mkdirs();
			if (!dirCreated) {
				System.err.println("Failed to create directory: " + directoryPath);
			}
		} else {
			System.out.println("Directory exists: " + directoryPath);
		}

		String filePath = directoryPath + gameId;

		// save data in json file
		Map<String, String> data = new HashMap<>();
		data.put("isPlayersTurn", "" + isPlayersTurn);
		data.put("lastPositionAttacked", Arrays.toString(lastPositionAttacked));
		data.put("enemyField", twoDimArrayToString(enemyField));
		data.put("friendlyField", twoDimArrayToString(friendlyField));
		data.put("fieldSize", "" + fieldSize);
		data.put("anzahlSchiffeGroesse5", "" + anzahlSchiffeGroesse5);
		data.put("anzahlSchiffeGroesse4", "" + anzahlSchiffeGroesse4);
		data.put("anzahlSchiffeGroesse3", "" + anzahlSchiffeGroesse3);
		data.put("anzahlSchiffeGroesse2", "" + anzahlSchiffeGroesse2);
		data.put("totalShipPartsCount", "" + totalShipPartsCount);
		data.put("isClientFieldSizeDone", "" + isClientFieldSizeDone);
		data.put("areClientFieldShipsDone", "" + areClientFieldShipsDone);
		data.put("isOpponentReady", "" + isOpponentReady);

		try {

			serializeHashMap(data, filePath);

			// inform via socket to the other player that game has been loaded successfully
			out.write(String.format("ok%n"));
			out.flush();

			return true;

		} catch (IOException e) {
			e.printStackTrace();

			return false;
		}

	}

	private Map<String, String> deSerializeHashMap(String filePath) throws ClassNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(filePath);
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) ois.readObject();
		ois.close();
		System.out.println("De Serialized HashMap data  saved in hashmap.ser");
		return map;
	}

	private void serializeHashMap(Map<String, String> hmap, String filePath) throws IOException {
		filePath = filePath.replace(".ser", ""); // if extension is included in filepath, remove it
		System.out.println("serializeHashMap(): save in " + filePath + ".ser");
		FileOutputStream fos = new FileOutputStream(filePath + ".ser");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(hmap);
		oos.close();
		fos.close();
		System.out.println("Serialized HashMap data is saved in " + filePath);
	}

	public static String twoDimArrayToString(int[][] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < array.length; i++) {
			sb.append(Arrays.toString(array[i]));
			if (i < array.length - 1) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	public static int[] parseStringToIntArray(String str) {
		// Remove brackets and split by commas
		String[] stringArray = str.replaceAll("\\[|\\]|\\s", "").split(",");
		int[] intArray = new int[stringArray.length];
		for (int i = 0; i < stringArray.length; i++) {
			intArray[i] = Integer.parseInt(stringArray[i]);
		}
		return intArray;
	}

	public static int[][] parseStringToInt2DArray(String str) {
		// Remove the outer brackets and split into rows
		String[] rows = str.substring(1, str.length() - 1).split("], \\[");

		// Adjust the first and last rows to remove extra brackets
		rows[0] = rows[0].substring(1);
		rows[rows.length - 1] = rows[rows.length - 1].substring(0, rows[rows.length - 1].length() - 1);

		int[][] array = new int[rows.length][];
		for (int i = 0; i < rows.length; i++) {
			array[i] = parseStringToIntArray("[" + rows[i] + "]");
		}
		return array;
	}

	private boolean loadGameClient(String gameId) {
		// TODO
		String currentDirectory = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		// Decode URL encoding
		currentDirectory = URLDecoder.decode(currentDirectory, StandardCharsets.UTF_8);

		// get file where games are saved as client
		String filePath = currentDirectory + "/savedGamesAsClient/" + gameId + ".ser";

		// get data from file
		try {
			Map<String, String> gameData = deSerializeHashMap(filePath);

			// extract game data
			this.isPlayersTurn = Boolean.parseBoolean(gameData.get("isPlayersTurn"));
			this.lastPositionAttacked = parseStringToIntArray(gameData.get("lastPositionAttacked"));
			this.enemyField = parseStringToInt2DArray(gameData.get("enemyField"));
			this.friendlyField = parseStringToInt2DArray(gameData.get("friendlyField"));
			this.fieldSize = Integer.parseInt(gameData.get("fieldSize"));
			this.anzahlSchiffeGroesse5 = Integer.parseInt(gameData.get("anzahlSchiffeGroesse5"));
			this.anzahlSchiffeGroesse4 = Integer.parseInt(gameData.get("anzahlSchiffeGroesse4"));
			this.anzahlSchiffeGroesse3 = Integer.parseInt(gameData.get("anzahlSchiffeGroesse3"));
			this.anzahlSchiffeGroesse2 = Integer.parseInt(gameData.get("anzahlSchiffeGroesse2"));
			this.totalShipPartsCount = Integer.parseInt(gameData.get("totalShipPartsCount"));
			this.isClientFieldSizeDone = Boolean.parseBoolean(gameData.get("isClientFieldSizeDone"));
			this.areClientFieldShipsDone = Boolean.parseBoolean(gameData.get("areClientFieldShipsDone"));
			this.isOpponentReady = Boolean.parseBoolean(gameData.get("isOpponentReady"));

			// inform via socket to the other player to save the game
			out.write(String.format("ok%n"));
			out.flush();

			return true;

		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
	}

	private void manageBattle() throws IOException {
		// Netzwerknachrichten lesen und verarbeiten.
		// Da die graphische Oberfläche von einem separaten Thread verwaltet
		// wird, kann man hier unabhängig davon auf Nachrichten warten.
		// Manipulationen an der Oberfläche sollten aber mittels invokeLater
		// (oder invokeAndWait) ausgeführt werden.
		while (true) {
			String line = this.in.readLine(); // read line from socket
			System.out.println("received: " + line);
			String[] responseList = line.split(" "); // split line based on whitespace

			// if save game message is received
			if (responseList[0].equals("save")) {
				boolean savingSuccesfull = saveGameClient(responseList[1]);

			}
			// if load game message is received
			else if (responseList[0].equals("load")) {
				boolean loadingSuccessfull = loadGameClient(responseList[1]);

			}
			// player is receiving an attack
			else if (responseList[0].equals("shot")) {
				// process attack
				int positionAttackedRow = Integer.parseInt(responseList[1]);
				int positionAttackedColumn = Integer.parseInt(responseList[2]);
				int attackResult = -1;

				// prepare answer according to the content of the attacked position
				switch (friendlyField[positionAttackedRow][positionAttackedColumn]) {
				case 0: // es gibt Wasser in indexAttacked - 1
				{
					attackResult = 0;
					friendlyField[positionAttackedRow][positionAttackedColumn] = 2;
					isPlayersTurn = true;
					// System.out.println(role+"'s turn now");
					break;
				}
				case 1: // es gibt Schieffteil in indexAttacked - 1
				{
					attackResult = 1;
					friendlyField[positionAttackedRow][positionAttackedColumn] = 3; // change to hit ship part
					isPlayersTurn = false;
					// System.out.println("NOT " + role + "'s turn now");

					boolean isAttackedShipDestroyed = isShipCompletelyDestroyed(positionAttackedRow,
							positionAttackedColumn);
					if (isAttackedShipDestroyed) {
						System.out.println("ship is completely destroyed");
						attackResult = 2;
						markDestroyedShip(positionAttackedRow, positionAttackedColumn);
					}

					break;
				}

				}

				// send answer
				// System.out.println("sent: " + String.format("answer %d%n", attackResult));
				out.write(String.format("answer %d%n", attackResult));
				out.flush();

				// check if player lost: count of ships (value = 1) is 0
				if (countValueOcurrencesInArray(friendlyField, 1) == 0) {
					System.out.println("lost.");
					isPlayersTurn = false;

				} else if (isPlayersTurn) // if player did not lose and is its turn, attack
				{
					attack();
				}

			}

			else if (responseList[0].equals("answer")) // get attack answer and process it
			{

				int attackAnswerNumber = Integer.parseInt(responseList[1]);

				System.out.println("Last possition attacked: " + Arrays.toString(lastPositionAttacked));

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
				case 2:
				// Schiff gesunken
				{
					enemyField[lastPositionAttacked[0]][lastPositionAttacked[1]] = 2;
					isPlayersTurn = true;

					markDestroyedEnemyShip(lastPositionAttacked[0], lastPositionAttacked[1]);

					break;
				}
				}

				// check if player won: count of hit ships (value = 2) is totalShipPartsCount
				System.out.print("State of enemy field: ");
				System.out.println(Arrays.deepToString(enemyField));
				System.out.println(
						"countValueOcurrencesInArray(enemyField, 3) = " + countValueOcurrencesInArray(enemyField, 3));
				System.out.println("totalShipPartsCount = " + totalShipPartsCount);
				if (countValueOcurrencesInArray(enemyField, 3) == totalShipPartsCount) {
					System.out.println("won.");
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
	}

	public void start() {
		try {
			indexToAttackNext = new int[] { 0, 0 };

			// role is always Client when the player is Computer with no UI. Server is
			// localhost
			isPlayersTurn = false; // Server starts playing

			manageSocketConnection();

			managePreparationBeforeBattle();

			manageBattle();

			// EOF ins Socket "schreiben" und das Programm explizit beenden
			// (weil es sonst weiterlaufen würde, bis der Benutzer das Hauptfenster
			// schließt).

			s.shutdownOutput();
			//Fully close Socket afterwards
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Connection closed.");
		System.exit(0);

	}

	public static void main(String[] args) throws IOException {
		PlayerBotNoUI spieler = new PlayerBotNoUI(5, 1, 0, 0, 0);

		spieler.start();
	}

}
