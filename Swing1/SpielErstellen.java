import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class SpielErstellen {
    // Graphische Oberfläche aufbauen und anzeigen.
    private static void start () {
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

	// Darunter ein horizontal zentriertes "Etikett" (JLabel)
	// hinzufügen.
	JLabel label = new JLabel("Spiel erstellen");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	frame.add(label);

	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	//frame.add(Box.createVerticalStrut(50));
	
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
			JLabel labelBreite = new JLabel("Breite");
			JTextField textFielBreite = new JTextField("Breite");
			textFielBreite.setMaximumSize(new Dimension(100, textFielBreite.getPreferredSize().height));
			JLabel labelHoehe = new JLabel("Höhe");
			JTextField textFielHoehe = new JTextField("Höhe");
			textFielHoehe.setMaximumSize(new Dimension(100, textFielHoehe.getPreferredSize().height));
			JLabel labelEmpty = new JLabel("asdf");
			
			panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			panel.add(labelBreite);
			panel.add(textFielBreite);
			panel.add(labelHoehe);
			panel.add(textFielHoehe);

		}
		
		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(5, 2, 5, 10));
		{
			JLabel labelGroesse5 = new JLabel("Größe \"5\"");
			JTextField textFielGroesse5 = new JTextField("Größe \"5\"");
			textFielGroesse5.setMaximumSize(new Dimension(200, textFielGroesse5.getPreferredSize().height));
			JLabel labelGroesse4 = new JLabel("Größe \"4\"");
			JTextField textFielGroesse4 = new JTextField("Größe \"4\"");
			textFielGroesse4.setMaximumSize(new Dimension(200, textFielGroesse4.getPreferredSize().height));
			JLabel labelGroesse3 = new JLabel("Größe \"3\"");
			JTextField textFielGroesse3 = new JTextField("Größe \"3\"");
			textFielGroesse3.setMaximumSize(new Dimension(200, textFielGroesse3.getPreferredSize().height));
			JLabel labelGroesse2 = new JLabel("Größe \"2\"");
			JTextField textFielGroesse2 = new JTextField("Größe \"2\"");
			textFielGroesse2.setMaximumSize(new Dimension(200, textFielGroesse2.getPreferredSize().height));
			JLabel labelGroesse1 = new JLabel("Größe \"1\"");
			JTextField textFielGroesse1 = new JTextField("Größe \"1\"");
			textFielGroesse1.setMaximumSize(new Dimension(200, textFielGroesse1.getPreferredSize().height));
			
			panel2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
			panel2.add(labelGroesse5);
			panel2.add(textFielGroesse5);
			panel2.add(labelGroesse4);
			panel2.add(textFielGroesse4);
			panel2.add(labelGroesse3);
			panel2.add(textFielGroesse3);
			panel2.add(labelGroesse2);
			panel2.add(textFielGroesse2);
			panel2.add(labelGroesse1);
			panel2.add(textFielGroesse1);
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
	buttonSpielErstellen.addActionListener(
	    (e) -> { System.out.println("Knopf gedrückt: Spiel starten"); }
	);
	frame.add(buttonSpielErstellen);
	
	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	frame.add(Box.createVerticalStrut(50));
	

	// Menüzeile (JMenuBar) erzeugen und einzelne Menüs (JMenu)
	// mit Menüpunkten (JMenuItem) hinzufügen.
	// Jeder Menüpunkt ist eigentlich ein Knopf, dem wie oben
	// eine anonyme Funktion zugeordnet werden kann.
	// (Hier exemplarisch nur für einen Menüpunkt.)
	JMenuBar bar = new JMenuBar();
	{
	    JMenu menu = new JMenu("Menü");
	    {
		JMenuItem item = new JMenuItem("Menü öffnen");
		item.addActionListener(
		    (e) -> { System.out.println("File -> Menü öffnen"); }
		);
		menu.add(item);
	    }
	    {
		JMenuItem item = new JMenuItem("Programm beenden");
		item.addActionListener(
			    (e) -> { 
			    	System.out.println("File -> Programm beenden"); 
			    	System.exit(0);
			    	}
			);
		menu.add(item);
	    }
	    bar.add(menu);
	}

	// Menüzeile zum Fenster hinzufügen.
	frame.setJMenuBar(bar);

	// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
	// und das Fenster anzeigen (setVisible).
	frame.pack();
	frame.setVisible(true);
    }

    // Hauptprogramm.
    public static void main (String [] args) {
	// Laut Swing-Dokumentation sollte die graphische Oberfläche
	// nicht direkt im Hauptprogramm (bzw. im Haupt-Thread) erzeugt
	// und angezeigt werden, sondern in einem von Swing verwalteten
	// separaten Thread.
	// Hierfür wird der entsprechende Code in eine parameterlose
	// anonyme Funktion () -> { ...... } "verpackt", die an
	// SwingUtilities.invokeLater übergeben wird.
	SwingUtilities.invokeLater(
	    () -> { start(); }
	);
    }
}
