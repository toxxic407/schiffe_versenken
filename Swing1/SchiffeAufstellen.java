import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Components.MenuBar;

public class SchiffeAufstellen {
	private BufferedReader in; // Verpackung des Socket-Eingabestroms.
	private Writer out; // Verpackung des Socket-Ausgabestroms.
	private String role;
	private JFrame mainFrame;
	private JFrame menuFrame;
	private int[][] field;
	private JPanel fieldPanel = new JPanel();
	private JPanel fieldGridPanel = new JPanel();
	private boolean playAgainstComputer;
	private boolean botWillPlayForMe;
	private int fieldSize;
	private int anzahlSchiffeGroesse5;
	private int anzahlSchiffeGroesse4;
	private int anzahlSchiffeGroesse3;
	private int anzahlSchiffeGroesse2;
//	private int sizeOfShipToRelocate = -1;
//	private boolean shipToRelocateShouldBeVertical = false;
	private boolean relocationOfShipMode = false;
//	private int startXShipToRelocate;
//	private int startYShipToRelocate;
	private Socket s;
	private boolean isClientFieldSizeDone;
	private boolean areClientFieldShipsDone;

	public SchiffeAufstellen(JFrame menuFrame, boolean playAgainstComputer, int fieldSize, int anzahlSchiffeGroesse5,
			int anzahlSchiffeGroesse4, int anzahlSchiffeGroesse3, int anzahlSchiffeGroesse2) {
		// set instance variables
		this.role = "Server";
		this.menuFrame = menuFrame;
		this.playAgainstComputer = playAgainstComputer;

		this.fieldSize = fieldSize;

		this.field = new int[fieldSize][fieldSize];
		this.anzahlSchiffeGroesse5 = anzahlSchiffeGroesse5;
		this.anzahlSchiffeGroesse4 = anzahlSchiffeGroesse4;
		this.anzahlSchiffeGroesse3 = anzahlSchiffeGroesse3;
		this.anzahlSchiffeGroesse2 = anzahlSchiffeGroesse2;

		showUI();

	}

	public SchiffeAufstellen(JFrame menuFrame, boolean playAgainstComputer) {
		/*
		 * Constructor for Client role
		 */
		// Client
		this.role = "Client";
		this.menuFrame = menuFrame;
		this.playAgainstComputer = playAgainstComputer;

		manageSocketConnection();

		askForFieldInformation();

		showUI();

	}

	private void manageSocketConnection() {
		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		boolean isConnectionSuccesfull = false;

		while (!isConnectionSuccesfull) {
			try {
				Socket s = new Socket("localhost", port);
				this.s = s;

				// Ein- und Ausgabestrom des Sockets ermitteln
				// und als BufferedReader bzw. Writer verpacken.
				this.in = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
				this.out = new OutputStreamWriter(this.s.getOutputStream());

				isConnectionSuccesfull = true;

			} catch (Exception e) {
				continue;
			}

		}

		System.out.println("Connection established.");
	}

	static int search(String[] arr, String s) {
		/* Returns count of occurrences of string s in arr[] */
		int counter = 0;
		for (int j = 0; j < arr.length; j++)

			if (s.equals(arr[j]))
				counter++;

		return counter;
	}

	private void askForFieldInformation() {
		// Preparation before battle
		// get field size
		while (!isClientFieldSizeDone) {
			try {
				System.out.println("Waiting for field size...");
				String line;

				line = in.readLine(); // read line from socket
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.field = new int[fieldSize][fieldSize];

		// get number of each ship size
		while (!areClientFieldShipsDone) {
			try {
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
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void spawnFieldWhenRelocatingShip(boolean shipToRelocateShouldBeVertical, int sizeOfShipToRelocate) {
		fieldPanel.remove(fieldGridPanel);
		fieldGridPanel.removeAll();
		fieldGridPanel = new JPanel();

		fieldGridPanel.setLayout(new GridLayout(fieldSize, fieldSize));

		// iterate over field and display buttons accordingly
		for (int i = 0; i < fieldSize; i++) {
			int row = i;

			for (int j = 0; j < fieldSize; j++) {
				int column = j;
				JButton button = new JButton(row + "," + column);
				button.setPreferredSize(new Dimension(50, 50));

				// if button is NOT in valid position to relocate ship, disable it. If it is in
				// valid position, add method to place the ship there
				boolean canShipBeRelocatedHere = canPlaceShip(row, column, sizeOfShipToRelocate,
						!shipToRelocateShouldBeVertical);

				if (!canShipBeRelocatedHere) {
					button.setEnabled(false);
				} else {

					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							relocationOfShipMode = false; // deactivate relocation mode
							placeShip(row, column, sizeOfShipToRelocate, !shipToRelocateShouldBeVertical, 1);
							spawnField(); // spawn field as normal

						}
					});

				}

				// change color of button according to value in the field (ship part or water)
				if (field[row][column] == 0 && canShipBeRelocatedHere) {
					// wasser
					button.setBackground(Color.blue);
				} else if (field[row][column] == 0) {
					button.setBackground(Color.red); // wasser, aber Ship can NOT be located here
				}

				if (field[row][column] == 1) {
					// Schiffteil
					button.setBackground(Color.black);
				}

				fieldGridPanel.add(button);
			}
		}

		fieldPanel.add(fieldGridPanel);

		// Revalidate and repaint the mainFrame to reflect changes
		fieldPanel.revalidate();
		fieldPanel.repaint();
	}

	private void manageRelocateShipManually(int row, int column) {

		this.relocationOfShipMode = true;

		// deactivate start game button

		int[] shipInfo = getShipInfo(row, column);
		/*
		 * for (int k = 0; k < shipInfo.length; k++) { System.out.println(shipInfo[k]);
		 * }
		 */
		int sizeOfShipToRelocate = shipInfo[0];
		boolean shipToRelocateShouldBeVertical = shipInfo[1] == 1;
		int startXShipToRelocate = shipInfo[2];
		int startYShipToRelocate = shipInfo[3];
		removeShip(shipInfo[2], shipInfo[3], shipToRelocateShouldBeVertical, sizeOfShipToRelocate);

		// TODO create method spawn field when relocating. It should enable only the
		// buttons where it is possible to relocate that ship size in that position
		spawnFieldWhenRelocatingShip(shipToRelocateShouldBeVertical, sizeOfShipToRelocate);

	}

	private void spawnField() {
		fieldPanel.remove(fieldGridPanel);
		fieldGridPanel.removeAll();
		fieldGridPanel = new JPanel();

		fieldGridPanel.setLayout(new GridLayout(fieldSize, fieldSize));

		// iterate over field and display buttons accordingly
		for (int i = 0; i < fieldSize; i++) {
			for (int j = 0; j < fieldSize; j++) {
				int row = i;
				int column = j;
				JButton button = new JButton(row + "," + column);
				button.setPreferredSize(new Dimension(50, 50));

				// change color of button according to value in the field (ship part or water)
				if (field[row][column] == 0) {
					// wasser
					button.setBackground(Color.blue);
				}
				if (field[row][column] == 1) {
					// Schiffteil
					button.setBackground(Color.black);
				}
				if (field[row][column] == 2) {
					// Schiffteil to be relocated
					button.setBackground(Color.gray);
				}
				if (field[row][column] == -1) {
					// Schiffteil to be relocated but in invalid position
					button.setBackground(Color.red);
				}

				// add functionality to change the position of the ship, on the buttons that
				// have a ship part.
				if (relocationOfShipMode == false && field[row][column] == 1) {
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							manageRelocateShipManually(row, column);

						}
					});
				}

				fieldGridPanel.add(button);
			}
		}

		fieldPanel.add(fieldGridPanel);

		// Revalidate and repaint the mainFrame to reflect changes
		fieldPanel.revalidate();
		fieldPanel.repaint();
		// fieldPanel.setVisible(true);
	}

	// Method to check if the new position is valid
//	private boolean isValidPosition(int row, int column, boolean vertical, int size) {
//		if (vertical) {
//			if (row + size > fieldSize)
//				return false; // Check boundaries
//			for (int i = 0; i < size; i++) {
//				if (field[row + i][column] != 0)
//					return false; // Check for collisions
//			}
//		} else {
//			if (column + size > fieldSize)
//				return false; // Check boundaries
//			for (int i = 0; i < size; i++) {
//				if (field[row][column + i] != 0)
//					return false; // Check for collisions
//			}
//		}
//		return true;
//	}

	private int[] getShipInfo(int x, int y) {
		int size = 0;
		boolean vertical = false;

		// Determine if the ship is vertical or horizontal
		if (x > 0 && field[x - 1][y] == 1 || x < fieldSize - 1 && field[x + 1][y] == 1) {
			vertical = true;
		}

		// Find the start of the ship
		int startX = x;
		int startY = y;
		if (vertical) {
			while (startX > 0 && field[startX - 1][y] == 1) {
				startX--;
			}
			// Calculate the size of the ship
			while (startX + size < fieldSize && field[startX + size][y] == 1) {
				size++;
			}
		} else {
			while (startY > 0 && field[x][startY - 1] == 1) {
				startY--;
			}
			// Calculate the size of the ship
			while (startY + size < fieldSize && field[x][startY + size] == 1) {
				size++;
			}
		}

		return new int[] { size, vertical ? 1 : 0, startX, startY };
	}

	// Method to remove a ship from the field
	private void removeShip(int startX, int startY, boolean vertical, int size) {
		if (vertical) {
			for (int i = startX; i < startX + size; i++) {
				field[i][startY] = 0;
			}
		} else {
			for (int j = startY; j < startY + size; j++) {
				field[startX][j] = 0;
			}
		}

	}

	private void placeAllShips() {
		this.field = new int[this.fieldSize][this.fieldSize];

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
					if (isInBounds(newRow, newCol) && field[newRow][newCol] != 0) {
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
					if (isInBounds(newRow, newCol) && field[newRow][newCol] != 0) {
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
				field[row][col + i] = shipValue;
			}
		} else {
			// Place the ship vertically
			for (int i = 0; i < shipSize; i++) {
				field[row + i][col] = shipValue;
			}
		}
	}

	private boolean isInBounds(int row, int col) {
		/**
		 * Method to check if a position is within the bounds of the field
		 */
		return row >= 0 && row < fieldSize && col >= 0 && col < fieldSize;
	}

	private void manageGameStarter() {
		// If Local player is Bot
		if (this.botWillPlayForMe) {
			if (this.role == "Server") {
				PlayerBot playerBot = new PlayerBot(this.menuFrame, this.field, this.anzahlSchiffeGroesse5,
						this.anzahlSchiffeGroesse4, this.anzahlSchiffeGroesse3, this.anzahlSchiffeGroesse2);
				playerBot.start();

			} else if (this.role == "Client") {
				// TODO when player is Client and bot
				PlayerBot playerBot = new PlayerBot(this.menuFrame, this.field, this.s);
				playerBot.start();
			}

		} else // If Local player is NOT Bot
		{
			if (this.role == "Server") {
				Player player = new Player(this.menuFrame, this.field, this.anzahlSchiffeGroesse5,
						this.anzahlSchiffeGroesse4, this.anzahlSchiffeGroesse3, this.anzahlSchiffeGroesse2);
				player.start();
			} else if (this.role == "Client") {
				// TODO when player is Client and bot
				Player player = new Player(this.menuFrame, this.field, this.s);
				player.start();
			}

		}

		// When Opponent player is BOT
		if (this.playAgainstComputer) {
			// Create Opponent Bot
			PlayerBotNoUI playerOpponentBot = new PlayerBotNoUI(this.field.length, anzahlSchiffeGroesse5,
					anzahlSchiffeGroesse4, anzahlSchiffeGroesse3, anzahlSchiffeGroesse2);

			new Thread(() -> {
				playerOpponentBot.start();
			}).start();

		}
	}

	private void showUI() {
		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		// "Swing1" wird in den Titelbalken geschrieben.
		mainFrame = new JFrame("Schiffe versenken");

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set Minimum size
		mainFrame.setMinimumSize(new Dimension(1200, 500));

		// Center the window on the screen
		this.mainFrame.setLocationRelativeTo(null);

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		mainFrame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		mainFrame.add(Box.createGlue());

		// Darunter ein horizontal zentriertes "Etikett" (JLabel)
		// hinzufügen.
		JLabel label = new JLabel("Schiffe aufstellen");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainFrame.add(label);

		mainFrame.add(Box.createVerticalStrut(50));

		// place ships in random positions
		placeAllShips();

		fieldPanel = new JPanel();
		spawnField();
		mainFrame.add(fieldPanel);

		mainFrame.add(Box.createVerticalStrut(50));

		JButton buttonSuffleShips = new JButton("Schiffe neu positionieren");
		buttonSuffleShips.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonSuffleShips.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Schiffe neu positionieren");

			this.relocationOfShipMode = false; // deactivate relocation mode

			// place ships in random positions
			placeAllShips();

			// respawn field
			spawnField();

		});
		mainFrame.add(buttonSuffleShips);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		mainFrame.add(Box.createVerticalStrut(50));

		JButton buttonSpielErstellen = new JButton("Spiel starten");
		buttonSpielErstellen.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonSpielErstellen.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Spiel starten");

			// is mode to relocate ship is activated, do not start game
			if (this.relocationOfShipMode) {
				JOptionPane.showMessageDialog(mainFrame,
						"Es ist nicht möglich, das Spiel zu starten, weil nicht alle Schiffe auf dem Spielfeld sind.", "Fehler",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// ask who will play: computer of bot

			String[] options = { "Ich", "Computer" };
			int result = JOptionPane.showOptionDialog(mainFrame, "Wer wird Spielen?", "Spieler wählen",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, // no custom icon
					options, // button titles
					options[0] // default button
			);

			if (result == JOptionPane.YES_OPTION) {
				this.botWillPlayForMe = false;
				System.out.println("Player will play");

			} else if (result == JOptionPane.NO_OPTION) {
				this.botWillPlayForMe = true;
				System.out.println("Computer will play");
			}

			if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
				mainFrame.setVisible(false);

				manageGameStarter();
			}

		});
		mainFrame.add(buttonSpielErstellen);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		mainFrame.add(Box.createVerticalStrut(50));

		// Menüzeile (JMenuBar) erzeugen und einzelne Menüs (JMenu)
		// mit Menüpunkten (JMenuItem) hinzufügen.
		// Jeder Menüpunkt ist eigentlich ein Knopf, dem wie oben
		// eine anonyme Funktion zugeordnet werden kann.
		// (Hier exemplarisch nur für einen Menüpunkt.)
		JMenuBar menuBar = new MenuBar(mainFrame, menuFrame);

		// Menüzeile zum Fenster hinzufügen.
		mainFrame.setJMenuBar(menuBar);

		// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
		// und das Fenster anzeigen (setVisible).
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

}
