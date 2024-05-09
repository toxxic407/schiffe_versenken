import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SpielTest {
    private static String role;		// Rolle: Server oder Client.
    private static BufferedReader in;	// Verpackung des Socket-Eingabestroms.
    private static Writer out;		// Verpackung des Socket-Ausgabestroms.
    private static boolean[][] enemyField = new boolean[3][64];
    private static boolean[][] friendlyField = new boolean[3][64];
    private static JPanel enemyGridPanel = new JPanel();
    private static JPanel friendlyGridPanel = new JPanel();
    private static JFrame mainFrame;
    private static JPanel enemyPanel;
    private static JPanel friendlyPanel;
    
    public static void spawnEnemyField() {
    	enemyPanel.remove(enemyGridPanel);
    	enemyGridPanel.removeAll();
		enemyGridPanel = new JPanel();
		enemyGridPanel.setLayout(new GridLayout(8,8));
		JButton[] button = new JButton[64];
		for(int i = 0; i<64;i++) {
			int index = i;
			
			JButton temp = new JButton(Integer.toString(i+1));
			temp.setFont(new Font("Arial", Font.PLAIN, 8));
			temp.setPreferredSize(new Dimension(45, 45));
			if(enemyField[0][i]) {
				temp.setBackground(Color.green);
			}
			if(enemyField[1][i]) {
				temp.setEnabled(false);    // deactivate button if it was deactivated before
			}
			if(enemyField[2][i]) {
				temp.setBackground(Color.black);
			}
			
			button[i] = temp;  
			// add action listener to send attacke
		    button[i].addActionListener(e -> {
		        // Access the variable 'index' here
		        try {
		            out.write(String.format("%d%n", index + 1));
		            out.flush();
		            
		            button[index].setEnabled(false);
		            enemyField[1][index] = true;
		            
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
		friendlyGridPanel.setLayout(new GridLayout(8,8));
		JButton[] button = new JButton[64];
		for(int i = 0; i<64;i++) {
			int index = i;
			
			JButton temp = new JButton(Integer.toString(i+1));
			temp.setFont(new Font("Arial", Font.PLAIN, 8));
			temp.setPreferredSize(new Dimension(45, 45));
			// friendly field is completely deactivated
			temp.setEnabled(false);
			if(friendlyField[0][i]) {
				temp.setBackground(Color.green);
			}
			if(friendlyField[1][i]) {
				temp.setEnabled(false);    // deactivate button if it was deactivated before
			}
			if(friendlyField[2][i]) {
				temp.setBackground(Color.black);
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
    
    // Graphische Oberfläche aufbauen und anzeigen.
    private static void startGui () {
    	
    	// make friendly field have at least one ship
    	// if random number is even, the field it has one structure, if not, another structure
    	Random rand = new Random();

    	// Obtain a number between [0 - 49].
    	int n = rand.nextInt(10);
        if (n % 2 == 0) {
	    	friendlyField[0][10] = true;
	    	friendlyField[0][11] = true;
	    	friendlyField[0][12] = true;
        }
	   else {
		   friendlyField[0][38] = true;
		   friendlyField[0][46] = true;
		   friendlyField[0][54] = true;
	    }

    	
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
		    String line = in.readLine();    // read line from socket
		    //System.out.println("input: " + line);
		    int indexAttacked = Integer.parseInt(line);     // parse index attacked
		    System.out.println("index attacked: " + indexAttacked);
		    friendlyField[2][indexAttacked-1] = true;    // set value of field that has been attacked
		    
		    
		    // reload field
		    spawnFriendlyField();

		    if (line == null) break;
		    /*
		    SwingUtilities.invokeLater(
			() -> { button.setEnabled(true); }
		    );
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
