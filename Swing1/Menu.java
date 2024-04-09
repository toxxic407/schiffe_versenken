import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Erstes Beispiel zur Verwendung von (AWT und) Swing.
class Menu {
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
	JLabel label = new JLabel("Spielmodus wählen");
	label.setAlignmentX(Component.CENTER_ALIGNMENT);
	frame.add(label);

	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	frame.add(Box.createVerticalStrut(50));

	// Horizontal zentrierten Knopf (JButton) hinzufügen.
	// Beim Drücken des Knopfs wird die an addActionListener
	// übergebene anonyme Funktion (e) -> { ...... } aufgerufen,
	// die einen Parameter des Typs ActionEvent besitzen muss,
	// der hier aber nicht verwendet wird und dessen Typ auch nicht
	// explizit angegeben werden muss.
	JButton buttonGegenSpieler = new JButton("Gegen anderen Spieler spielen");
	buttonGegenSpieler.setAlignmentX(Component.CENTER_ALIGNMENT);
	buttonGegenSpieler.addActionListener(
	    (e) -> { System.out.println("Knopf gedrückt: Gegen anderen Spieler spielen"); }
	);
	frame.add(buttonGegenSpieler);

	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	frame.add(Box.createVerticalStrut(50));
	
	// Button: Gegen den Computer Spielen
	JButton buttonGegenComputer = new JButton("Gegen den Computer spielen");
	buttonGegenComputer.setAlignmentX(Component.CENTER_ALIGNMENT);
	buttonGegenComputer.addActionListener(
	    (e) -> { System.out.println("Knopf gedrückt: Gegen den Computer spielen"); }
	);
	frame.add(buttonGegenComputer);

	// Festen Zwischenraum der Größe 50 Pixel hinzufügen.
	frame.add(Box.createVerticalStrut(50));
	
	// Button: Speil laden
	// Button: Gegen den Computer Spielen
	JButton buttonSpielLaden = new JButton("Speil laden");
	buttonSpielLaden.setAlignmentX(Component.CENTER_ALIGNMENT);
	buttonSpielLaden.addActionListener(
	    (e) -> { System.out.println("Knopf gedrückt: Spiel laden"); }
	);
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
