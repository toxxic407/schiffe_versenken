import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Components.MenuBar;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class SpielFinden {
	private JFrame frame;
	private JFrame menuFrame;
	private JPanel serversTablePanel;
	private DefaultTableModel tableModel;
	private JScrollPane scrollPane;

	// Graphische Oberfläche aufbauen und anzeigen.
	public SpielFinden(JFrame menuFrame) {
		this.menuFrame = menuFrame;

		showUI();

	}

	private void manageJoinGame() {
		// Find the selected row
		int selectedRow = -1;
		for (int i = 0; i < this.tableModel.getRowCount(); i++) {
			// get value of first column of the table. It indicates whether the IP-Address
			// has been selected or not
			boolean isIpSelected = (boolean) this.tableModel.getValueAt(i, 0);
			if (isIpSelected) {
				selectedRow = i;
				break;
			}
		}
		// If a row is selected, join the game using the IP address from that row
		if (selectedRow != -1) {
			String selectedIp = (String) this.tableModel.getValueAt(selectedRow, 1);
			System.out.println("Joining game at IP: " + selectedIp);

			// TODO Add logic to join the game using the selected IP address
			

		} else {
			// Show a warning message if no game is selected
			JOptionPane.showMessageDialog(frame, "Please select a game to join.", "No Game Selected",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private String[] scanIpAddresses() throws UnknownHostException {
		byte[] my_ip = InetAddress.getLocalHost().getAddress();
		my_ip[3] = 0;
		String firstIpInTheNetwork = InetAddress.getByAddress(my_ip).toString().substring(1);

		ExecutorService executorService = Executors.newFixedThreadPool(20);
		final String networkId = firstIpInTheNetwork.substring(0, firstIpInTheNetwork.length() - 1);
		ConcurrentSkipListSet<String> ipsSet = new ConcurrentSkipListSet();

		AtomicInteger ips = new AtomicInteger(0);
		while (ips.get() <= 254) {
			String ip = networkId + ips.getAndIncrement();
			executorService.submit(() -> {
				try {
					InetAddress inAddress = InetAddress.getByName(ip);
					if (inAddress.isReachable(500)) {
						ipsSet.add(ip);
					}
				} catch (IOException e) {

				}
			});
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}

		String[] simpleArray = new String[ipsSet.size()];
		ipsSet.toArray(simpleArray);
		
		// replace first element of ipsSet with "localhost"
		simpleArray[0] = "localhost";
		
		return simpleArray;
	}

	private Object[][] formatIPAddressesArray(String[] ipArray) {
		/*
		 * Transform Array of string to an Object that can be used for the table Example
		 * of final format: Object[][] data = { { false, "192.0.2.0" }, { false,
		 * "192.0.2.1" }, { false, "192.0.2.3" } };
		 * 
		 * Note: This excludes the first element form ipArray, because it is assumed
		 * that it contains the ip of the local machine
		 */

		Object[][] formatedData = new Object[ipArray.length][2];

		for (int i = 0; i < ipArray.length; i++) {
			formatedData[i][0] = false;
			formatedData[i][1] = ipArray[i];
		}

		System.out.println("formatIPAddressesArray(): " + Arrays.deepToString(formatedData));

		return formatedData;
	}

	private void displayAvailableServers() {

		class IpFinder extends SwingWorker<String[], Object> {
			String[] ipAddresses;

			@Override
			public String[] doInBackground() throws UnknownHostException {
				// display empty table without IPs while the user is waiting
				spawnTable(new Object[0][0]);

				this.ipAddresses = scanIpAddresses();
				return ipAddresses;
			}

			@Override
			public void done() {
				// format Arrays of IP addresses
				Object[][] data = formatIPAddressesArray(this.ipAddresses);
				spawnTable(data);
			}
		}

		IpFinder ipfinder = new IpFinder();
		ipfinder.execute();

	}

	private void spawnTable(Object[][] data) {
		if (scrollPane != null) {
			System.out.println("Removing scrollpane from serversTablePanel");
			serversTablePanel.remove(scrollPane);
			scrollPane.removeAll();
		}

		// TODO remove later: Table test data
		// Define column names for the table

		// Table model and JTable
		String[] columnNames = { "Wählen", "IP-Adresse" };
		this.tableModel = new DefaultTableModel(data, columnNames) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				// The first column contains Boolean values (checkboxes)
				if (columnIndex == 0) {
					return Boolean.class;
				} else {
					return String.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				// Only the first column (checkboxes) is editable
				return column == 0;
			}
		};

		// Create the table with the model
		JTable table = new JTable(tableModel);

		// Add mouse listener to handle checkbox selection logic
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Get the row that was clicked
				int row = table.rowAtPoint(e.getPoint());

				// Iterate through all rows to uncheck checkboxes
				for (int i = 0; i < table.getRowCount(); i++) {
					tableModel.setValueAt(false, i, 0);
				}

				// Check the clicked checkbox
				tableModel.setValueAt(true, row, 0);

			}
		});

		// Set the preferred width for the columns
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.setRowHeight(25);

		// Create a scroll pane to hold the table and add it to the center of the layout
		scrollPane = new JScrollPane(table);
		scrollPane.setMaximumSize(new Dimension(500, 200));

		// add new scrollpane to serversTablePanel
		serversTablePanel.add(scrollPane, BorderLayout.CENTER);

		// Revalidate and repaint the frame to reflect changes
		serversTablePanel.revalidate();
		serversTablePanel.repaint();
	}

	private void showUI() {
		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		// "Swing1" wird in den Titelbalken geschrieben.
		frame = new JFrame("Schiffe versenken");

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set Minimum size
		frame.setMinimumSize(new Dimension(1200, 500));

		// Center the window on the screen
		frame.setLocationRelativeTo(null);

		// Use BorderLayout for the window layout
		frame.setLayout(new BorderLayout());

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		frame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		frame.add(Box.createGlue());

		// Darunter ein horizontal zentriertes "Etikett" (JLabel)
		// hinzufügen.
		JLabel label = new JLabel("Verfügbare Spiele");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.add(label);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(20));

		serversTablePanel = new JPanel();
		displayAvailableServers();
		frame.add(serversTablePanel);

		// Festen Zwischenraum der Größe 25 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(25));

		// Create a panel to hold the buttons
		JPanel buttonPanel = new JPanel();

		// Button re-scan ip-addresses
		JButton rescanButton = new JButton("Erneut scannen");
		rescanButton.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Erneut scannen");
			displayAvailableServers();
		});

		// Create the "Neues Spiel erstellen" button
		JButton createGameButton = new JButton("Neues Spiel erstellen");
		createGameButton.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Neues Spiel erstellen");
			frame.setVisible(false); // this will close current login box window
			new SpielErstellen("Server", menuFrame, false); // display windows to create game, playAgainstComputer =
															// true
		});

		// Create the "Spiel beitreten" button
		JButton joinGameButton = new JButton("Spiel beitreten");
		joinGameButton.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Spiel beitreten");
			manageJoinGame();
		});

		// Add the buttons to the button panel
		buttonPanel.add(rescanButton);
		buttonPanel.add(createGameButton);
		buttonPanel.add(joinGameButton);
		// Add the button panel to the bottom of the layout
		frame.add(buttonPanel, BorderLayout.SOUTH);

		// Festen Zwischenraum der Größe 25 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(25));

		// Menüzeile (JMenuBar) erzeugen und einzelne Menüs (JMenu)
		// mit Menüpunkten (JMenuItem) hinzufügen.
		// Jeder Menüpunkt ist eigentlich ein Knopf, dem wie oben
		// eine anonyme Funktion zugeordnet werden kann.
		// (Hier exemplarisch nur für einen Menüpunkt.)
		JMenuBar menuBar = new MenuBar(frame, menuFrame);

		// Menüzeile zum Fenster hinzufügen.
		frame.setJMenuBar(menuBar);

		// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
		// und das Fenster anzeigen (setVisible).
		frame.pack();
		frame.setVisible(true);
	}

	/*
	 * // Hauptprogramm. public static void main (String [] args) { // Laut
	 * Swing-Dokumentation sollte die graphische Oberfläche // nicht direkt im
	 * Hauptprogramm (bzw. im Haupt-Thread) erzeugt // und angezeigt werden, sondern
	 * in einem von Swing verwalteten // separaten Thread. // Hierfür wird der
	 * entsprechende Code in eine parameterlose // anonyme Funktion () -> { ...... }
	 * "verpackt", die an // SwingUtilities.invokeLater übergeben wird.
	 * SwingUtilities.invokeLater( () -> { start(); } ); }
	 * 
	 */

}