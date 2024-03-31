import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.*;
public class Swing1 {
	
	static public JFrame f = new JFrame();
	public static JPanel connection_panel = new JPanel();
	public static JPanel side_panel = new JPanel();
	public static JPanel method_panel = new JPanel();
	public static boolean ipselector_is_shown = false;
	public static boolean turn = true;
	
	public static void main(String[] args) throws IOException {
		f.setLayout(new FlowLayout());
		
		
		f.setSize(1000,1000);
		f.setVisible(true);
		spawn_connection_selector();
		spawn_field();
		spawn_side_panel();
		SwingUtilities.updateComponentTreeUI(f);
	}
	
	public static void spawn_ip_selector() throws IOException, InterruptedException, ExecutionException {
		
		//Swing worker to stop gui freeze and show waiting screen during ip discovery
		class ipFinder extends SwingWorker<String[], Object> {
	        @Override
	       public String[] doInBackground() throws UnknownHostException {
	           return scan();
	       }

	        @Override
	       protected void done() {
	        	//fix for race condition where another connection mode is selected before doInBackground has finished
	        	if(ipselector_is_shown) {
	        		try {
	        			JComboBox selector = new JComboBox(get());
	        			JLabel label = new JLabel("Select your server:");
	        			JButton button=new JButton("Join");
	       		
	        			connection_panel.remove(method_panel);
	        			method_panel = new JPanel();
	        			method_panel.setBackground(Color.green);
	    				method_panel.setPreferredSize(new Dimension(500, 100));
	       		
	        			method_panel.add(label);
	        			method_panel.add(selector);
	        			method_panel.add(button);
	        			connection_panel.add(method_panel);
	        			SwingUtilities.updateComponentTreeUI(f);
	        		} catch (Exception ignore) {
	        		}
	        	}
	       }
	   }

	   (new ipFinder()).execute();
		   
		JLabel wait = new JLabel("Waiting for Network Scan to finish...");
		method_panel.add(wait);
	
	}
	
	public static void spawn_manual() throws IOException {
		JLabel label = new JLabel("Enter server address:");
		JTextField ip = new JTextField();
		JButton button=new JButton("Join");//creating instance of JButton  
		
		method_panel.add(label);
		method_panel.add(ip);
		method_panel.add(button);
	
	}
	
	public static void spawn_host_info() throws IOException {
		//Show IP
		byte[] ip = InetAddress.getLocalHost().getAddress();
		JLabel label = new JLabel("IP: "+ InetAddress.getByAddress(ip).toString().substring(1));
		JButton button=new JButton("Start Game");
		
		method_panel.add(label);
		method_panel.add(button);
	
	}
	
	public static void spawn_connection_selector() throws IOException {
		
		connection_panel.setLayout(new FlowLayout());
		
		String[] connectiontypes = {"Host","Select IP","Enter address manually"}; 
		JPanel panel = new JPanel();
		panel.setBackground(Color.blue);
		panel.setPreferredSize(new Dimension(400, 100));
		JComboBox method_selector = new JComboBox(connectiontypes);
		JLabel label = new JLabel("Select your connection type:");
		panel.add(label);
		panel.add(method_selector);
		connection_panel.add(panel);
		
		//spawn initial option
		method_panel.setBackground(Color.green);
		method_panel.setPreferredSize(new Dimension(400, 100));
		spawn_host_info();
		connection_panel.add(method_panel);
		method_selector.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	connection_panel.remove(method_panel);
		    	method_panel = new JPanel();
		    	method_panel.setBackground(Color.green);
				method_panel.setPreferredSize(new Dimension(500, 100));
		    	try {
		    		switch(method_selector.getSelectedItem().toString()) {
		    			case "Host":
		    				ipselector_is_shown = false;
		    				spawn_host_info();
		    				break;
		    			case "Select IP":
		    				ipselector_is_shown = true;
		    				spawn_ip_selector();
		    				break;
		    			case "Enter address manually":
		    				ipselector_is_shown = false;
		    				spawn_manual();
		    				break;
		    			default:
		    				spawn_host_info();
		    		}
		    	} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	connection_panel.add(method_panel);
		    	SwingUtilities.updateComponentTreeUI(f);
		    }
		});
		
		f.add(connection_panel);
	
	}
	
	public static String[] scan() throws UnknownHostException {
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
                }
                catch (IOException e) {

                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        String[] simpleArray = new String[ ipsSet.size() ];
	    ipsSet.toArray( simpleArray );
	    return simpleArray;
    }

	public static void spawn_field() {
		JPanel grid_panel = new JPanel();
		grid_panel.setLayout(new GridLayout(8,8));
		JButton[] button = new JButton[64];
		for(int i = 0; i<64;i++) {
			JButton temp = new JButton(Integer.toString(i+1));
			temp.setPreferredSize(new Dimension(80, 80));
			button[i] = temp;
			button[i].addActionListener(new ActionListener() {
				@Override
			    public void actionPerformed(ActionEvent e) {
			        turn = !turn;
			        f.remove(side_panel);
			        spawn_side_panel();
			    }
			});
			grid_panel.add(button[i]);
		}
		f.add(grid_panel);
	}
	
	public static void spawn_side_panel() {
		side_panel = new JPanel();
		side_panel.setLayout(new FlowLayout());
		JLabel turntext;
		if(turn) {
			turntext = new JLabel("Your Turn!");
			turntext.setForeground(Color.green);
		}else {
			turntext = new JLabel("Opponents Turn!");
			turntext.setForeground(Color.red);
		}
		side_panel.add(turntext);
		f.add(side_panel);
		SwingUtilities.updateComponentTreeUI(f);
	}
}