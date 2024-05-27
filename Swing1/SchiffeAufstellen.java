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
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Components.MenuBar;

public class SchiffeAufstellen {
	private String role;
	private JFrame mainFrame;
	private JFrame menuFrame;
	private int[][] field;
	private JPanel fieldPanel = new JPanel();
	private JPanel fieldGridPanel = new JPanel();
	private boolean playAgainstComputer;
	private boolean botWillPlay;
	private int fieldSize;
	private int anzahlSchiffeGroesse5;
	private int anzahlSchiffeGroesse4;
	private int anzahlSchiffeGroesse3;
	private int anzahlSchiffeGroesse2;
	private int sizeOfShipToRelocate = -1;
	private boolean shipToRelocateShouldBeVertical = false;
	private int startXShipToRelocate;
	private int startYShipToRelocate;
	private Socket s;

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
				if (sizeOfShipToRelocate == -1 && field[row][column] == 1) {
					button.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							int[] shipInfo = getShipInfo(row, column);
							/*
							 * for (int k = 0; k < shipInfo.length; k++) { System.out.println(shipInfo[k]);
							 * }
							 */
							sizeOfShipToRelocate = shipInfo[0];
							shipToRelocateShouldBeVertical = shipInfo[1] == 1;
							startXShipToRelocate = shipInfo[2];
							startYShipToRelocate = shipInfo[3];
							removeShip(shipInfo[2], shipInfo[3], shipToRelocateShouldBeVertical, sizeOfShipToRelocate);

						}
					});
				} else if (sizeOfShipToRelocate > 0 && field[row][column] == 0) {
					// TODO add functionality to the button to set the new position of the ship.
					// Show a preview of where would the ship be located (value = 2)
					// remove the preview when user stops hovering on the button

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
	private boolean isValidPosition(int row, int column, boolean vertical, int size) {
		if (vertical) {
			if (row + size > fieldSize)
				return false; // Check boundaries
			for (int i = 0; i < size; i++) {
				if (field[row + i][column] != 0)
					return false; // Check for collisions
			}
		} else {
			if (column + size > fieldSize)
				return false; // Check boundaries
			for (int i = 0; i < size; i++) {
				if (field[row][column + i] != 0)
					return false; // Check for collisions
			}
		}
		return true;
	}

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

		spawnField(); // Refresh the field to reflect changes
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

	public SchiffeAufstellen(String role, JFrame menuFrame, boolean playAgainstComputer, boolean botWillPlay,
			int fieldSize, int anzahlSchiffeGroesse5, int anzahlSchiffeGroesse4, int anzahlSchiffeGroesse3,
			int anzahlSchiffeGroesse2) {
		// set instance variables
		this.role = role;

		if (role.equals("Server")) {
			this.menuFrame = menuFrame;
			this.playAgainstComputer = playAgainstComputer;
			this.botWillPlay = botWillPlay;
			this.fieldSize = fieldSize;

			this.field = new int[fieldSize][fieldSize];
			this.anzahlSchiffeGroesse5 = anzahlSchiffeGroesse5;
			this.anzahlSchiffeGroesse4 = anzahlSchiffeGroesse4;
			this.anzahlSchiffeGroesse3 = anzahlSchiffeGroesse3;
			this.anzahlSchiffeGroesse2 = anzahlSchiffeGroesse2;

		} else {
			// when role is Client, create socket connection and get information from Server

		}

		showUI();

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

		JButton buttonSpielErstellen = new JButton("Spiel starten");
		buttonSpielErstellen.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonSpielErstellen.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Spiel starten");

			mainFrame.setVisible(false);

			if (this.playAgainstComputer) {
				if (!this.botWillPlay) {
					if (this.role == "Server") {

						new Player(this.field, this.anzahlSchiffeGroesse5, this.anzahlSchiffeGroesse4,
								this.anzahlSchiffeGroesse3, this.anzahlSchiffeGroesse2).start();

					} else if (this.role == "Client") {
						// TODO when player is Client and NOT bot

					}

				} else if (botWillPlay) {
					if (this.role == "Server") {
						// TODO when player is server and bot

					} else if (this.role == "Client") {
						// TODO when player is Client and bot
					}
				}
			}

			/*
			 * new SchiffeAufstellen( menuFrame, playAgainstComputer, botWillPlay,
			 * this.fieldSize, this.anzahlSchiffeGroesse5, this.anzahlSchiffeGroesse4,
			 * this.anzahlSchiffeGroesse3, this.anzahlSchiffeGroesse2 );
			 */
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
