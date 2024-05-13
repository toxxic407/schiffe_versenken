import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SpielTest {
	private static String role; // Rolle: Server oder Client.
	private static BufferedReader in; // Verpackung des Socket-Eingabestroms.
	private static Writer out; // Verpackung des Socket-Ausgabestroms.
	private static boolean isPlayersTurn;
	private static int lastPositionAttacked;
	/*
	 * Werte für enemyField: 0 = unbekant, 1 = geschossen und Wasser gefunden, 2 =
	 * geschossen und Schief getroffen
	 */
	private static int[] enemyField = new int[64];
	/*
	 * Werte für friendlyField: 0 = Wasser, 1 = Schiefteil intakt, 2 = Wasser
	 * Geschossen, 3 = Schiefteil getroffen
	 */
	private static int[] friendlyField = new int[64];
	private static JPanel enemyGridPanel = new JPanel();
	private static JPanel friendlyGridPanel = new JPanel();
	private static JFrame mainFrame;
	private static JPanel enemyPanel;
	private static JPanel friendlyPanel;
	private static int totalShipPartsCount;

	public static void spawnEnemyField() {
		enemyPanel.remove(enemyGridPanel);
		enemyGridPanel.removeAll();
		enemyGridPanel = new JPanel();
		enemyGridPanel.setLayout(new GridLayout(8, 8));
		JButton[] button = new JButton[64];
		for (int i = 0; i < 64; i++) {
			int index = i;

			//JButton temp = new JButton(Integer.toString(i + 1));
			JButton temp = new JButton();
			temp.setFont(new Font("Arial", Font.PLAIN, 8));
			temp.setPreferredSize(new Dimension(45, 45));

			// if it is not players turn, deactivate button to attack
			if (isPlayersTurn == false) {
				temp.setEnabled(false);
			}
			else {
				temp.setEnabled(true);
			}

			if (enemyField[i] != 0) {
				// schon geschossen
				temp.setEnabled(false); // deactivate button
			}
			if (enemyField[i] == 1) {
				// gechossen aber wasser
				temp.setBackground(Color.blue);
			}
			if (enemyField[i] == 2) {
				// getroffen
				temp.setBackground(Color.red);
			}

			button[i] = temp;
			// add action listener to send attacke
			button[i].addActionListener(e -> {
				// Access the variable 'index' here
				try {

					lastPositionAttacked = index; // save last attacked index

					System.out.println(String.format("sent: shot %d%n", index + 1));

					// send attack
					out.write(String.format("shot %d%n", index + 1));
					out.flush();

				} catch (IOException ex) {
					System.out.println("write to socket failed");
				}
			});

			enemyGridPanel.add(button[i]);
		}

		enemyPanel.add(enemyGridPanel);

		// Revalidate and repaint the frame to reflect changes
		enemyPanel.revalidate();
		enemyPanel.repaint();
	}

	public static void spawnFriendlyField() {
		friendlyPanel.remove(friendlyGridPanel);
		friendlyGridPanel.removeAll();
		friendlyGridPanel = new JPanel();
		friendlyGridPanel.setLayout(new GridLayout(8, 8));
		JButton[] button = new JButton[64];
		for (int i = 0; i < 64; i++) {
			int index = i;

			//JButton temp = new JButton(Integer.toString(i + 1));
			JButton temp = new JButton();
			temp.setFont(new Font("Arial", Font.PLAIN, 8));
			temp.setPreferredSize(new Dimension(45, 45));
			// friendly field is completely deactivated
			temp.setEnabled(false);

			if (friendlyField[i] == 1) {
				// es gibt Schieffteil da
				temp.setBackground(Color.green);
			}
			if (friendlyField[i] == 2) {
				// Wasser geschossen
				temp.setBackground(Color.blue);
			}
			if (friendlyField[i] == 3) {
				// Schiefteil getroffen
				temp.setBackground(Color.red);
			}

			button[i] = temp;
			// add action listener to send attacke

			friendlyGridPanel.add(button[i]);
		}
		friendlyPanel.add(friendlyGridPanel);

		// Revalidate and repaint the frame to reflect changes
		friendlyPanel.revalidate();
		friendlyPanel.repaint();
	}
	
	private static int countValueOcurrencesInArray(int[] arrayToAnalyze, int valueToFind) {
	    int output = 0;
	    for (int i = 0; i < arrayToAnalyze.length; i++)
	        if (valueToFind == arrayToAnalyze[i])
	        	output++;
	    return output;
	}

	// Graphische Oberfläche aufbauen und anzeigen.
	private static void startGui() {

		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		mainFrame = new JFrame(role);

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		mainFrame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		mainFrame.add(Box.createGlue());

		// add enemy's field
		JLabel label1 = new JLabel("Gegnerisches Spielfeld");
		label1.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainFrame.add(label1);

		enemyPanel = new JPanel();
		spawnEnemyField();
		mainFrame.add(enemyPanel);

		// add player's field
		JLabel label2 = new JLabel("Ihres Spielfeld");
		label2.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainFrame.add(label2);

		friendlyPanel = new JPanel();
		spawnFriendlyField();
		mainFrame.add(friendlyPanel);

		// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
		// und das Fenster anzeigen (setVisible).
		mainFrame.pack();
		mainFrame.setVisible(true);

	}

	public static void main(String[] args) throws IOException {
		
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
		if (args.length == 0) {
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

			s = new Socket(args[0], port);
		}
		System.out.println("Connection established.");

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new OutputStreamWriter(s.getOutputStream());

		// Graphische Oberfläche aufbauen.
		SwingUtilities.invokeLater(() -> {
			startGui();
		});

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
					System.out.println(role+"'s turn now");
					break;
				}
				case 1: // es gibt Schieffteil in indexAttacked - 1
				{
					attackResult = 1;
					friendlyField[positionAttacked - 1] = 3;	// change to hit ship part
					isPlayersTurn = false;
					System.out.println("NOT " + role + "'s turn now");
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
					System.out.println(role + " lost.");
					isPlayersTurn = false;
					spawnFriendlyField();
					spawnEnemyField();
					JOptionPane.showMessageDialog(mainFrame, "You lost :(");
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
					System.out.println("NOT " + role + "'s turn anymore");
					break;
				}
				case 1: // geschossen und Schief getroffen
				{
					enemyField[lastPositionAttacked] = 2;
					isPlayersTurn = true;
					System.out.println("Still " +role+"'s turn");
					break;
				}

				}
				
				// reload fields
				spawnFriendlyField();
				spawnEnemyField();
				
				// check if player won: count of hit ships (value = 2) is totalShipPartsCount
				if (countValueOcurrencesInArray(enemyField, 2) == totalShipPartsCount) {
					System.out.println(role + " won.");
					isPlayersTurn = false;
					spawnFriendlyField();
					spawnEnemyField();
					JOptionPane.showMessageDialog(mainFrame, "You won! :)");
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
