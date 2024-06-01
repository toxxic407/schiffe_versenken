import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

class Menu {
	// TODO close sockets when game ends for all 3 player types (also when any
	// player gets disconnected). So that other games can be created
	// TODO use Swing Workers instead of Thread()
	// TODO make the size of the buttons on the field scale according to the number
	// of rows and columns (smaller rows and columns -> bigger buttons, the opposite
	// when bigger rows and columns. Or a standard size that is ok even on the
	// maximum field size (30 * 30)
	// TODO solve bug: when loading game, some parts of the ships of the opponent
	// are in different positions (at least when playing against computer)
	// TODO in SpielErstellen, add a verification process to check if the field size
	// is between 5 and 30. Also add process to verify if it is possible to place
	// all the ships according to the Field size. For example: it is impossible to
	// place 3 ships of size 5 in a field of size 5 * 5, so the program should
	// display an error message

	// Graphische Oberfläche aufbauen und anzeigen.

	public static void start() {
		// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
		// "Swing1" wird in den Titelbalken geschrieben.
		JFrame frame = new JFrame("Schiffe versenken");

		// Beim Schließen des Fensters (z. B. durch Drücken des
		// X-Knopfs in Windows) soll das Programm beendet werden.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set Minimum size
		frame.setMinimumSize(new Dimension(1200, 500));

		// Center the window on the screen
		frame.setLocationRelativeTo(null);

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

			// TODO redirect to Server finder / SpielFinden
			new SpielFinden(frame);

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

			// SpielTestNew spielTestNew = new SpielTestNew("");
			// SpielComputerNoUI spielComputerNoUI = new SpielComputerNoUI("localhost");

			// startGameFrame(spielTestNew);

			// startGameFrameNoUI(spielComputerNoUI);

			new SpielErstellen("Server", frame, true);

		});
		frame.add(buttonGegenComputer);

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
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON file(.json)", "json");

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