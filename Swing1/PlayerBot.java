import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Components.MenuBar;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

public class PlayerBot {
	private String role; // Rolle: Server oder Client.
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
	private int fieldSize;
	private int anzahlSchiffeGroesse5 = 0;
	private int anzahlSchiffeGroesse4 = 0;
	private int anzahlSchiffeGroesse3 = 0;
	private int anzahlSchiffeGroesse2 = 0;
	private JPanel enemyGridPanel = new JPanel();;
	private JPanel friendlyGridPanel = new JPanel();;
	private JFrame mainFrame;
	private JFrame menuFrame;
	private JPanel enemyPanel;
	private JPanel friendlyPanel;
	private int totalShipPartsCount;
	private boolean isClientFieldSizeDone = false;
	private boolean areClientFieldShipsDone = false;
	private boolean isOpponentReady = false;
	private Socket s;
	public ServerSocket ss;

	public PlayerBot(JFrame menuFrame, int[][] field, int anzahlSchiffeGroesse5, int anzahlSchiffeGroesse4,
			int anzahlSchiffeGroesse3, int anzahlSchiffeGroesse2) {
		/*
		 * Constructor for Server role
		 */
		this.role = "Server";
		this.friendlyField = field;
		this.fieldSize = field.length;
		this.anzahlSchiffeGroesse5 = anzahlSchiffeGroesse5;
		this.anzahlSchiffeGroesse4 = anzahlSchiffeGroesse4;
		this.anzahlSchiffeGroesse3 = anzahlSchiffeGroesse3;
		this.anzahlSchiffeGroesse2 = anzahlSchiffeGroesse2;

		this.enemyField = new int[field.length][field.length];

		this.menuFrame = menuFrame;

		System.out.println(Arrays.deepToString(enemyField));
		System.out.println(Arrays.deepToString(friendlyField));

	}

	public PlayerBot(JFrame menuFrame, int[][] field, Socket s) {
		/*
		 * Constructor for Client role
		 */
		this.role = "Client";
		this.friendlyField = field;
		this.fieldSize = field.length;
		this.s = s;

		this.enemyField = new int[field.length][field.length];

		this.menuFrame = menuFrame;

		System.out.println(Arrays.deepToString(enemyField));
		System.out.println(Arrays.deepToString(friendlyField));
	}

	private void spawnEnemyField() {
		enemyPanel.remove(enemyGridPanel);
		enemyGridPanel.removeAll();
		enemyGridPanel = new JPanel();
		enemyGridPanel.setLayout(new GridLayout(this.friendlyField.length, this.friendlyField.length));
		for (int i = 0; i < friendlyField.length; i++) {
			int row = i;
			for (int j = 0; j < friendlyField.length; j++) {
				int column = j;

				// JButton temp = new JButton(Integer.toString(i + 1));
				JButton button = new JButton();
				button.setFont(new Font("Arial", Font.PLAIN, 8));
				button.setPreferredSize(new Dimension(45, 45));
				button.setEnabled(false); // all buttons always disabled

				if (enemyField[row][column] != 0) {
					// schon geschossen
					button.setEnabled(false); // deactivate button
				}
				if (enemyField[row][column] == 1) {
					// gechossen aber wasser
					button.setBackground(Color.blue);
				}
				if (enemyField[row][column] == 2) {
					// getroffen
					button.setBackground(Color.orange);
				}
				if (enemyField[row][column] == 3) {
					// getroffen
					button.setBackground(Color.red);
				}

				enemyGridPanel.add(button);
			}

		}

		enemyPanel.add(enemyGridPanel);

		// Revalidate and repaint the frame to reflect changes
		enemyPanel.revalidate();
		enemyPanel.repaint();
	}

	private void spawnFriendlyField() {
		friendlyPanel.remove(friendlyGridPanel);
		friendlyGridPanel.removeAll();
		friendlyGridPanel = new JPanel();
		friendlyGridPanel.setLayout(new GridLayout(this.friendlyField.length, this.friendlyField.length));
		for (int i = 0; i < enemyField.length; i++) {
			for (int j = 0; j < enemyField.length; j++) {
				// JButton temp = new JButton(Integer.toString(i + 1));
				JButton button = new JButton();
				button.setFont(new Font("Arial", Font.PLAIN, 8));
				button.setPreferredSize(new Dimension(45, 45));
				// friendly field is completely deactivated
				button.setEnabled(false);

				if (friendlyField[i][j] == 1) {
					// es gibt Schieffteil da
					button.setBackground(Color.green);
				}
				if (friendlyField[i][j] == 2) {
					// Wasser geschossen
					button.setBackground(Color.blue);
				}
				if (friendlyField[i][j] == 3) {
					// Schiefteil getroffen
					button.setBackground(Color.orange);
				}
				if (friendlyField[i][j] == 4) {
					// Schiefteil getroffen
					button.setBackground(Color.red);
				}

				friendlyGridPanel.add(button);
			}

		}
		friendlyPanel.add(friendlyGridPanel);

		// Revalidate and repaint the frame to reflect changes
		friendlyPanel.revalidate();
		friendlyPanel.repaint();
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

	// Graphische Oberfläche aufbauen und anzeigen.
	private void startGui() {
		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		this.mainFrame = new JFrame(role);

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set Minimum size
		this.mainFrame.setMinimumSize(new Dimension(1200, 500));

		// Center the window on the screen
		this.mainFrame.setLocationRelativeTo(null);

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		this.mainFrame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		this.mainFrame.add(Box.createGlue());

		// Create a panel with GridLayout to arrange enemy and friendly fields
		// horizontally
		JPanel fieldsPanel = new JPanel(new GridLayout(1, 2));

		// Enemy field
		JPanel enemyContainer = new JPanel(new BorderLayout());
		JLabel enemyLabel = new JLabel("Gegnerisches Spielfeld", JLabel.CENTER);
		enemyPanel = new JPanel();
		spawnEnemyField();
		enemyContainer.add(enemyLabel, BorderLayout.NORTH);
		enemyContainer.add(enemyPanel, BorderLayout.CENTER);

		// Friendly field
		JPanel friendlyContainer = new JPanel(new BorderLayout());
		JLabel friendlyLabel = new JLabel("Ihr Spielfeld", JLabel.CENTER);
		friendlyPanel = new JPanel();
		spawnFriendlyField();
		friendlyContainer.add(friendlyLabel, BorderLayout.NORTH);
		friendlyContainer.add(friendlyPanel, BorderLayout.CENTER);

		// Add both fields to the fieldsPanel
		fieldsPanel.add(enemyContainer);
		fieldsPanel.add(friendlyContainer);

		// Add the fieldsPanel to the main frame
		this.mainFrame.add(fieldsPanel);

		if (menuFrame != null) {
			// Menüzeile (JMenuBar) erzeugen und einzelne Menüs (JMenu)
			// mit Menüpunkten (JMenuItem) hinzufügen.
			// Jeder Menüpunkt ist eigentlich ein Knopf, dem wie oben
			// eine anonyme Funktion zugeordnet werden kann.
			// (Hier exemplarisch nur für einen Menüpunkt.)
			JMenuBar menuBar = new MenuBar(mainFrame, menuFrame);

			// Menüzeile zum Fenster hinzufügen.
			mainFrame.setJMenuBar(menuBar);
		}

		// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
		// und das Fenster anzeigen (setVisible).
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);

		// Center the window on the screen
		mainFrame.setLocationRelativeTo(null);

	}

	private String getShipsSettingAsString() {
		String out = "";

		if (anzahlSchiffeGroesse5 > 0) {
			out += "5 ".repeat(anzahlSchiffeGroesse5);
		}

		if (anzahlSchiffeGroesse4 > 0) {
			out += "4 ".repeat(anzahlSchiffeGroesse4);
		}

		if (anzahlSchiffeGroesse3 > 0) {
			out += "3 ".repeat(anzahlSchiffeGroesse3);
		}

		if (anzahlSchiffeGroesse2 > 0) {
			out += "2 ".repeat(anzahlSchiffeGroesse2 - 1);
			out += "2".repeat(1);
		}

		return out;
	}

	public void start() {
		/*new Thread(() -> {
			try {
				runGame();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();*/
		
		class GameWorker extends SwingWorker<Void, Void>
		{
		    protected Void doInBackground() throws Exception
		    {
		        try {
					runGame();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
		    }
		}

		new GameWorker().execute();
	}

	private void manageSocketConnection() throws IOException {

		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		// Socketverbindung zur anderen "Seite" herstellen.
		if (this.role == "Server") {
			isPlayersTurn = true; // Server starts playing

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

			this.ss = new ServerSocket(port);
			this.s = ss.accept();

			System.out.println("Connection established.");
		} else {

			isPlayersTurn = false; // Client does not start playing

			System.out.println();
			System.out.println();
		}

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		this.in = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
		this.out = new OutputStreamWriter(this.s.getOutputStream());
	}

	private void managePreparationBeforeBattle() throws IOException {

		// Preparation before battle
		// Side: Server
		if (this.role == "Server") // from Server perspective
		{
			// inform size of the field
			out.write(String.format("size %d%n", friendlyField.length));
			out.flush();

			System.out.println("Sent size");

			// wait until Server gets answer "done"
			while (!this.isClientFieldSizeDone) {
				System.out.println("Waiting for first done response");
				String line = in.readLine(); // read line from socket
				System.out.println("received: " + line);
				String[] responseList = line.split(" "); // split line based on whitespace
				if (responseList[0].equals("done")) {
					this.isClientFieldSizeDone = true;

				}
			}

			// inform about number of ship of each size
			out.write(String.format("ships " + getShipsSettingAsString() + " %n"));
			out.flush();

			// wait until Server gets answer "done"
			while (!this.areClientFieldShipsDone) {
				System.out.println("Waiting for second done response");
				String line = in.readLine(); // read line from socket
				System.out.println("received: " + line);
				String[] responseList = line.split(" "); // split line based on whitespace
				if (responseList[0].equals("done")) {
					this.areClientFieldShipsDone = true;
				}
			}

			// inform that server is ready
			// inform about number of ship of each size
			out.write(String.format("ready %n"));
			out.flush();

			// wait until opponent is ready
			while (!isOpponentReady) {
				String line = in.readLine(); // read line from socket
				System.out.println("received: " + line);
				String[] responseList = line.split(" "); // split line based on whitespace

				if (responseList[0].equals("ready")) {
					this.isOpponentReady = true;

					// refresh GUI since opponent is ready and Server can send Attack
					// reload fields
					spawnFriendlyField();
					spawnEnemyField();

				}

			}

		} else if (role == "Client") {
			// Side: Client

			this.isClientFieldSizeDone = true;
			this.areClientFieldShipsDone = true;

			// wait until server informs that it is ready
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

		System.out.println("Both players are ready");
		JOptionPane.showMessageDialog(mainFrame, "Der andere Spieler ist bereit.");
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
		System.out.println(Arrays.deepToString(enemyField));
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

	private boolean saveGameClient(String gameId) {
		// create folder to save game if it does not exist
		String directoryPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
				+ "/savedGamesAsClient/";
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
		System.out.println("Deserialized HashMap data  saved in hashmap.ser");
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

			// reload fields
			spawnEnemyField();
			spawnFriendlyField();

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

		System.out.println("is " + role + " turn: " + isPlayersTurn);

		// Send first attack if role is Server and isPlayersTurn
		if (this.role.equals("Server") && isPlayersTurn) {
			attack();

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

			// if save game message is received
			if (responseList[0].equals("save")) {
				boolean savingSuccesfull = saveGameClient(responseList[1]);

				if (savingSuccesfull) {
					// show success message
					JOptionPane.showMessageDialog(mainFrame,
							"Der Status wird automatisch gespeichert, weil ein anderer Spieler ihn gespeichert hat.");
				} else {
					String message = "Fehler beim Speichern des Spielstands";
					JOptionPane.showMessageDialog(new JFrame(), message, "Fehler", JOptionPane.ERROR_MESSAGE);
				}
			}
			// if load game message is received
			else if (responseList[0].equals("load")) {
				boolean loadingSuccessfull = loadGameClient(responseList[1]);

				if (loadingSuccessfull) {
					JOptionPane.showMessageDialog(mainFrame, "Spielstand erfolgreich geladen");
				} else {
					String message = "Fehler beim Laden des Spielstands";
					JOptionPane.showMessageDialog(new JFrame(), message, "Fehler", JOptionPane.ERROR_MESSAGE);
				}
			}
			// player is receiving an attack
			else
			// player is receiving an attack
			if (responseList[0].equals("shot")) {
				// process attack
				int positionAttackedRow = Integer.parseInt(responseList[1]);
				int positionAttackedColumn = Integer.parseInt(responseList[2]);
				int positionAttacked = Integer.parseInt(responseList[1]);
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
					// System.out.println("NOT " + role +"'s turn now");

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
				out.write(String.format("answer %d%n", attackResult));
				out.flush();

				// reload fields
				spawnFriendlyField();
				spawnEnemyField();

				// check if player lost: count of ships (value = 1) is 0
				if (countValueOcurrencesInArray(friendlyField, 1) == 0) {
					// System.out.println(role + " lost.");
					isPlayersTurn = false;
					spawnFriendlyField();
					spawnEnemyField();
					JOptionPane.showMessageDialog(mainFrame, "Du hast verloren :(");
				} else if (isPlayersTurn) // if player did not lose and is its turn, attack
				{
					attack();
				}

			}

			else if (responseList[0].equals("answer")) // get attack answer and process it
			{

				int attackAnswerNumber = Integer.parseInt(responseList[1]);

				switch (attackAnswerNumber) {
				case 0: // Wasser geschossen
				{
					enemyField[lastPositionAttacked[0]][lastPositionAttacked[1]] = 1;
					isPlayersTurn = false;
					// System.out.println("NOT " + role +"'s turn anymore");
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

				// reload fields
				spawnFriendlyField();
				spawnEnemyField();

				// check if player won: count of hit ships (value = 2) is totalShipPartsCount
				if (countValueOcurrencesInArray(enemyField, 3) == totalShipPartsCount) {
					// System.out.println(role + " won."); isPlayersTurn = false;
					spawnFriendlyField();
					spawnEnemyField();
					JOptionPane.showMessageDialog(this.mainFrame, "Du hast gewonnen! :)");
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

	private void runGame() throws IOException {

		// Graphische Oberfläche aufbauen.
		SwingUtilities.invokeLater(() -> {
			startGui();
		});

		// inform the user that the field will be activated when the other player is
		// ready
		JOptionPane.showMessageDialog(mainFrame, "Das Spiel wird starten, wenn der andere Spieler bereit ist.");

		// register total number of ship parts
		totalShipPartsCount = countValueOcurrencesInArray(friendlyField, 1);

		manageSocketConnection();

		managePreparationBeforeBattle();

		manageBattle();

		// EOF ins Socket "schreiben" und das Programm explizit beenden // (weil es
		// sonst weiterlaufen würde, bis der Benutzer das Hauptfenster // schließt).
		this.s.shutdownOutput();
		//Fully close Socket afterwards
		this.s.close();
		this.ss.close();
		System.out.println("Connection closed.");
		System.exit(0);

	}

	public static void main(String[] args) throws IOException {
		try {
			// Create client player for testing purposes
			final int port = 50000;
			Socket s = new Socket("localhost", port);

			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			Writer out = new OutputStreamWriter(s.getOutputStream());

			// get answer from server
			String line0 = in.readLine(); // read line from socket
			System.out.println("received line0: " + line0);

			// send done
			out.write(String.format("done %n"));
			out.flush();

			// get answer from server
			String line1 = in.readLine(); // read line from socket
			System.out.println("received line1: " + line1);

			// send done
			out.write(String.format("done %n"));
			out.flush();

			System.out.println("Client side message: game can start");

			int[][] field = new int[][] { { 1, 1, 1, 1, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 0, 0 } };
			new PlayerBot(null, field, s).start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
