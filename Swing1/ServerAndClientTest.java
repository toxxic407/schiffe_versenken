import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ServerAndClientTest {
    private static String role;		// Rolle: Server oder Client.
    private static BufferedReader in;	// Verpackung des Socket-Eingabestroms.
    private static Writer out;		// Verpackung des Socket-Ausgabestroms.
    private static JButton button;	// Der o. g. Knopf.
    
    // Graphische Oberfläche aufbauen und anzeigen.
    private static void startGui () {
	// Hauptfenster mit Titelbalken etc. (JFrame) erzeugen.
	JFrame frame = new JFrame(role);

	// Beim Schließen des Fensters (z. B. durch Drücken des
	// X-Knopfs in Windows) soll das Programm beendet werden.
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	// Der Inhalt des Fensters soll von einem BoxLayout-Manager
	// verwaltet werden, der seine Bestandteile vertikal (von
	// oben nach unten) anordnet.
	frame.setContentPane(Box.createVerticalBox());

	// Dehnbaren Zwischenraum am oberen Rand hinzufügen.
	frame.add(Box.createGlue());
	
	JTextField textField1 = new JTextField("message to send");
	textField1.setAlignmentX(Component.CENTER_ALIGNMENT);
	frame.add(textField1);
	
	
	// Horizontal zentrierten Knopf (JButton) hinzufügen.
	button = new JButton("Send");
	button.setAlignmentX(Component.CENTER_ALIGNMENT);
	button.addActionListener(
	    // Wenn der Knopf gedrückt wird,
	    // erfolgt eine Kontrollausgabe auf System.out.
	    // Anschließend wird der Knopf deaktiviert
	    // und eine beliebige Nachricht an die andere "Seite" geschickt,
	    // damit diese ihren Knopf aktivieren kann.
	    (e) -> {
		System.out.println(role);
		String message = textField1.getText();
		button.setEnabled(false);
		try {
		    out.write(String.format("%s%n", message));
		    out.flush();
		}
		catch (IOException ex) {
		    System.out.println("write to socket failed");
		}
	    }
	);
	frame.add(button);

	// Der Server-Knopf soll anfangs deaktiviert sein.
	if (role.equals("Server")) button.setEnabled(false);

	// Dehnbaren Zwischenraum am unteren Rand hinzufügen.
	frame.add(Box.createGlue());

	// Am Schluss (!) die optimale Fenstergröße ermitteln (pack)
	// und das Fenster anzeigen (setVisible).
	frame.pack();
	frame.setVisible(true);
	
    }
    
	public static void main(String[] args) throws IOException {
		// Verwendete Portnummer (vgl. Server.java).
		final int port = 50000;

		// Socketverbindung zur anderen "Seite" herstellen.
		Socket s;
		if (args.length == 0) {
		    role = "Server";

		    // Die eigene(n) IP-Adresse(n) ausgeben,
		    // damit der Benutzer sie dem Benutzer des Clients mitteilen kann.
		    System.out.print("My IP address(es):");
		    Enumeration<NetworkInterface> nis =
					    NetworkInterface.getNetworkInterfaces();
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
		}
		else {
		    role = "Client";
		    s = new Socket(args[0], port);
		}
		System.out.println("Connection established.");

		// Ein- und Ausgabestrom des Sockets ermitteln
		// und als BufferedReader bzw. Writer verpacken.
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new OutputStreamWriter(s.getOutputStream());

		// Graphische Oberfläche aufbauen.
		SwingUtilities.invokeLater(
		    () -> { startGui(); }
		);

		// Netzwerknachrichten lesen und verarbeiten.
		// Da die graphische Oberfläche von einem separaten Thread verwaltet
		// wird, kann man hier unabhängig davon auf Nachrichten warten.
		// Manipulationen an der Oberfläche sollten aber mittels invokeLater
		// (oder invokeAndWait) ausgeführt werden.
		while (true) {
		    String line = in.readLine();
		    System.out.println(line);
		    if (line == null) break;
		    SwingUtilities.invokeLater(
			() -> { button.setEnabled(true); }
		    );
		}

		// EOF ins Socket "schreiben" und das Programm explizit beenden
		// (weil es sonst weiterlaufen würde, bis der Benutzer das Hauptfenster 
		// schließt).
		s.shutdownOutput();
		System.out.println("Connection closed.");
		System.exit(0);
		
	}

}
