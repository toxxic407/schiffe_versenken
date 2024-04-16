import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import Components.MenuBar;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class SpielFinden {
    // Graphische Oberfläche aufbauen und anzeigen.
    public SpielFinden (JFrame menuFrame, boolean playAgainstComputer) {
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
	JLabel label = new JLabel("Spiel finden");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	frame.add(label);

	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	frame.add(Box.createVerticalStrut(50));

	Box box = Box.createHorizontalBox();
	{
	    JLabel labelIP = new JLabel("IP-Addresse: ");
	    JTextField textFieldIP = new JTextField("IP-Addresse"); 
	    // Set maximum size for textFieldIP
	    textFieldIP.setMaximumSize(new Dimension(200, textFieldIP.getPreferredSize().height));
	    box.add(Box.createHorizontalStrut(50));
		JButton buttonSpielBeitreten = new JButton("Spiel beitreten");
		buttonSpielBeitreten.addActionListener(
		    (e) -> { System.out.println("Knopf gedrückt: Spiel beitreten"); }
		);
	    
	    box.add(labelIP);  // Add label to the box
	    //box.add(Box.createGlue());
	    box.add(textFieldIP);  // Add textFieldIP to the box
	   // box.add(Box.createGlue());
	    box.add(buttonSpielBeitreten);
	}
	frame.add(box);
	
	frame.add(Box.createGlue());
	
	
	/*
	Box box = Box.createHorizontalBox();
	{
	    JLabel labelIP = new JLabel("IP-Addresse: ");
	    
	    box.add(labelIP);  // Add label to the box
	}
	box.add(Box.createHorizontalStrut(20));
	{
		JTextField textFieldIP = new JTextField("IP-Addresse");
		textFieldIP.setMaximumSize(new Dimension(200, textFieldIP.getPreferredSize().height));
		box.add(textFieldIP);  // Add textFieldIP to the box
	}
	box.add(Box.createHorizontalStrut(20));
	{
		JButton buttonSpielBeitreten = new JButton("Spiel beitreten");
		buttonSpielBeitreten.addActionListener(
		    (e) -> { System.out.println("Knopf gedrückt: Spiel beitreten"); }
		);
		
		box.add(buttonSpielBeitreten);
	}
	box.add(Box.createHorizontalGlue());
	frame.add(box);

	// Zwischenraum der Breite 50 oder mehr.
	//frame.add(Box.createHorizontalStrut(50));
	//frame.add(Box.createVerticalStrut(50));
	//frame.add(Box.createHorizontalGlue());
	*/

	JButton buttonSpielErstellen = new JButton("Neues Spiel erstellen");
	buttonSpielErstellen.setAlignmentX(Component.CENTER_ALIGNMENT);
	buttonSpielErstellen.addActionListener(
	    (e) -> { 
	    	System.out.println("Knopf gedrückt: Neues Spiel erstellen"); 
	    	frame.setVisible(false);   // this will close current login box window
	    	new SpielErstellen(menuFrame, playAgainstComputer);    // display windows to create game, playAgainstComputer = true
	    	}
	);
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

    /*
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
    
    */
}
