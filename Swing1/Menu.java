import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class Menu {
	// Graphische Oberfläche aufbauen und anzeigen.

	/*
	public static void startGameFrame(SpielTestNew spielTestFrame) {
		(new Thread() {
			@Override
			public void run() {
				try {
					spielTestFrame.start();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}).start();
	}

	public static void startGameFrameNoUI(SpielComputerNoUI spielNoUI) {
		(new Thread() {
			@Override
			public void run() {
				try {
					spielNoUI.start();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}).start();
	}
	*/

	public static void start() {
		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		// "Swing1" wird in den Titelbalken geschrieben.
		JFrame frame = new JFrame("Schiffe versenken");

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set Minimum size
		frame.setMinimumSize(new Dimension(1200, 500));

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		frame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		frame.add(Box.createGlue());

		// Button: Gegen anderen Spieler spielen
		JButton buttonGegenSpieler = new JButton("Gegen anderen Speieler spielen");
		buttonGegenSpieler.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonGegenSpieler.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Gegen anderen Speieler spielen");

			frame.setVisible(false); // this will close current login box window
			
			// TODO redirect to Server finder

		});
		frame.add(buttonGegenSpieler);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(50));

		// Button: Gegen den Computer Spielen
		JButton buttonGegenComputer = new JButton("Gegen den Computer spielen");
		buttonGegenComputer.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonGegenComputer.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Gegen den Computer spielen");

			frame.setVisible(false); // this will close current login box window

			//SpielTestNew spielTestNew = new SpielTestNew("");
			//SpielComputerNoUI spielComputerNoUI = new SpielComputerNoUI("localhost");

			//startGameFrame(spielTestNew);

			//startGameFrameNoUI(spielComputerNoUI);
			
			new SpielErstellen("Server", frame, true);

		});
		frame.add(buttonGegenComputer);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(50));

		// Button: Speil laden
		JButton buttonSpielLaden = new JButton("Spiel laden");
		buttonSpielLaden.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonSpielLaden.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Spiel laden");

			// choose file to load game
			File gameFilePath = getGameFilePath();
		});
		frame.add(buttonSpielLaden);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(50));

		// Horizontale Box hinzufügen, die ihrerseits aus drei
		// "Etiketten" (JLabel) besteht, die jeweils ein Piktogramm
		// (ImageIcon) enthalten. Dehnbarer Zwischenraum vor und nach
		// den "Etiketten" sorgt für eine gleichmäßige horizontale
		// Verteilung innerhalb der Box.

		// Dehnbaren Zwischenraum am unteren Rand hinzufügen.
		frame.add(Box.createGlue());

		// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
		// und das Fenster anzeigen (setVisible).
		frame.pack();
		frame.setVisible(true);
	}

	private static File getGameFilePath() {
		try {
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Game files (.json)", "json");

			fileChooser.setFileFilter(filter);

			fileChooser.setCurrentDirectory(new File("."));

			int result = fileChooser.showOpenDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = new File(fileChooser.getSelectedFile().getAbsolutePath());
				System.out.println("Filepath: " + selectedFile);
				return selectedFile;
			}

			return null;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}

	// Hauptprogramm.
	public static void main(String[] args) {
		// Laut Swing-Dokumentation sollte die graphische Oberfläche
		// nicht direkt im Hauptprogramm (bzw. im Haupt-Thread) erzeugt
		// und angezeigt werden, sondern in einem von Swing verwalteten
		// separaten Thread.
		// Hierfür wird der entsprechende Code in eine parameterlose
		// anonyme Funktion () -> { ...... } "verpackt", die an
		// SwingUtilities.invokeLater übergeben wird.
		SwingUtilities.invokeLater(() -> {
			start();
		});
	}
}