package Components;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar {

	public MenuBar(JFrame currentFrame, JFrame menuFrame) {
		{
			JMenu menu = new JMenu("Menü");
			{
				JMenuItem item = new JMenuItem("Menü öffnen");
				item.addActionListener((e) -> {
					System.out.println("File -> Menü öffnen");
					currentFrame.dispose(); // hide current window
					menuFrame.setVisible(true); // show Menu window

				});
				menu.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Programm beenden");
				item.addActionListener((e) -> {
					System.out.println("File -> Programm beenden");
					System.exit(0);
				});
				menu.add(item);
			}
			this.add(menu);
		}
	}
	

}
