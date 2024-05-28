import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

import Components.MenuBar;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class SpielErstellen {
	private JFrame frame;
	private int fieldSize;
	private int anzahlSchiffeGroesse5;
	private int anzahlSchiffeGroesse4;
	private int anzahlSchiffeGroesse3;
	private int anzahlSchiffeGroesse2;
	private JTextField txtfldSize;
	private JTextField textFielGroesse5;
	private JTextField textFielGroesse4;
	private JTextField textFielGroesse3;
	private JTextField textFielGroesse2;

	private boolean isStringAnInteger(String text) {
		try {
			int number = Integer.parseInt(text);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setFieldSetting() {
		// manage field size
		if (isStringAnInteger(txtfldSize.getText())) {
			this.fieldSize = Integer.parseInt(txtfldSize.getText());

		} else {
			JOptionPane.showMessageDialog(frame, "Feldgröße soll Integer sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
		}

		// manage number of ships size 5
		if (isStringAnInteger(textFielGroesse5.getText())) {
			this.anzahlSchiffeGroesse5 = Integer.parseInt(textFielGroesse5.getText());
		} else {
			JOptionPane.showMessageDialog(frame, "Anzahl der Schiffe (Größe 5) soll Integer sein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}

		// manage number of ships size 4
		if (isStringAnInteger(textFielGroesse4.getText())) {
			this.anzahlSchiffeGroesse4 = Integer.parseInt(textFielGroesse4.getText());
		} else {
			JOptionPane.showMessageDialog(frame, "Anzahl der Schiffe (Größe 4) soll Integer sein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}

		// manage number of ships size 3
		if (isStringAnInteger(textFielGroesse3.getText())) {
			this.anzahlSchiffeGroesse3 = Integer.parseInt(textFielGroesse3.getText());
		} else {
			JOptionPane.showMessageDialog(frame, "Anzahl der Schiffe (Größe 3) soll Integer sein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}

		// manage number of ships size 2
		if (isStringAnInteger(textFielGroesse2.getText())) {
			this.anzahlSchiffeGroesse2 = Integer.parseInt(textFielGroesse2.getText());
		} else {
			JOptionPane.showMessageDialog(frame, "Anzahl der Schiffe (Größe 2) soll Integer sein!", "Fehler",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	// Graphische Oberfläche aufbauen und anzeigen.
	public SpielErstellen(String role, JFrame menuFrame, boolean playAgainstComputer) {
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

		// Der Inhalt des Fensters soll von einem BoxLayout-Manager
		// verwaltet werden, der seine Bestandteile vertikal (von
		// oben nach unten) anordnet.
		frame.setContentPane(Box.createVerticalBox());

		// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
		frame.add(Box.createGlue());

		// Darunter ein horizontal zentriertes "Etikett" (JLabel)
		// hinzufügen.
		JLabel label = new JLabel("Spiel erstellen");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		frame.add(label);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		// frame.add(Box.createVerticalStrut(50));

		JPanel panelTitles = new JPanel();
		panelTitles.setLayout(new GridLayout(1, 2, 5, 10));

		{
			JLabel labelTitle1 = new JLabel("Wählen Sie die Größe des Spielfelds");
			JLabel labelTitle2 = new JLabel("Wählen Sie die Anzahl der Schiffe");

			panelTitles.add(labelTitle1);
			panelTitles.add(labelTitle2);
		}
		// add margin to panelTitles
		panelTitles.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
		frame.add(panelTitles);

		Box boxForForm = Box.createHorizontalBox();
		{

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(3, 2, 10, 50));
			{
				JLabel labelSize = new JLabel("Breite un Höhe");
				txtfldSize = new JTextField("5"); // 10
				txtfldSize.setMaximumSize(new Dimension(50, txtfldSize.getPreferredSize().height));

				panel.add(labelSize);
				panel.add(txtfldSize);

			}

			JPanel panel2 = new JPanel();
			panel2.setLayout(new GridLayout(5, 2, 5, 10));
			{
				JLabel labelGroesse5 = new JLabel("Größe \"5\"");
				textFielGroesse5 = new JTextField("1"); // 1
				textFielGroesse5.setMaximumSize(new Dimension(200, textFielGroesse5.getPreferredSize().height));
				JLabel labelGroesse4 = new JLabel("Größe \"4\"");
				textFielGroesse4 = new JTextField("0"); // 3
				textFielGroesse4.setMaximumSize(new Dimension(200, textFielGroesse4.getPreferredSize().height));
				JLabel labelGroesse3 = new JLabel("Größe \"3\"");
				textFielGroesse3 = new JTextField("0"); // 2
				textFielGroesse3.setMaximumSize(new Dimension(200, textFielGroesse3.getPreferredSize().height));
				JLabel labelGroesse2 = new JLabel("Größe \"2\"");
				textFielGroesse2 = new JTextField("0"); // 1
				textFielGroesse2.setMaximumSize(new Dimension(200, textFielGroesse2.getPreferredSize().height));

				panel2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

				panel2.add(labelGroesse5);
				panel2.add(textFielGroesse5);
				panel2.add(labelGroesse4);
				panel2.add(textFielGroesse4);
				panel2.add(labelGroesse3);
				panel2.add(textFielGroesse3);
				panel2.add(labelGroesse2);
				panel2.add(textFielGroesse2);
			}

			boxForForm.add(Box.createHorizontalStrut(50));
			boxForForm.add(panel);
			boxForForm.add(Box.createHorizontalStrut(90));
			boxForForm.add(panel2);
			boxForForm.add(Box.createHorizontalStrut(50));
		}

		frame.add(boxForForm);

		frame.add(Box.createGlue());

		JButton buttonSpielErstellen = new JButton("Spiel starten");
		buttonSpielErstellen.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonSpielErstellen.addActionListener((e) -> {
			System.out.println("Knopf gedrückt: Spiel starten");

			// ask who will play: computer of bot
			boolean botWillPlay = false;

			String[] options = { "Ich", "Computer" };
			int result = JOptionPane.showOptionDialog(frame, "Wer wird Spielen?", "Spieler wählen",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, // no custom icon
					options, // button titles
					options[0] // default button
			);

			if (result == JOptionPane.YES_OPTION) {
				botWillPlay = false;
				System.out.println("Player will play");

			} else if (result == JOptionPane.NO_OPTION) {
				botWillPlay = true;
				System.out.println("Computer will play");
			}

			// set field setting
			setFieldSetting();

			frame.setVisible(false);

			// go to SchiffeAufstellen
			new SchiffeAufstellen(role, menuFrame, playAgainstComputer, botWillPlay, this.fieldSize,
					this.anzahlSchiffeGroesse5, this.anzahlSchiffeGroesse4, this.anzahlSchiffeGroesse3,
					this.anzahlSchiffeGroesse2);

		});
		frame.add(buttonSpielErstellen);

		// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
		frame.add(Box.createVerticalStrut(50));

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

}