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
	public static JPanel method_panel = new JPanel();
	public static boolean ipselector_is_shown = false;
	
	public static void main(String[] args) throws IOException {
		f.setLayout(new FlowLayout());
		
		
		f.setSize(1000,1000);
		f.setVisible(true);
		spawn_connection_selector();
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
	       		
	        			f.remove(method_panel);
	        			method_panel = new JPanel();
	       		
	        			method_panel.add(label);
	        			method_panel.add(selector);
	        			method_panel.add(button);
	        			f.add(method_panel);
	        			SwingUtilities.updateComponentTreeUI(f);
	        		} catch (Exception ignore) {
	        		}
	        	}
	       }
	   }

	   (new ipFinder()).execute();
		   
		method_panel = new JPanel();
		JLabel wait = new JLabel("Waiting for Network Scan to finish...");
		method_panel.add(wait);
		f.add(method_panel);
	
	}
	
	public static void spawn_manual() throws IOException {
		method_panel = new JPanel();
		JLabel label = new JLabel("Enter server address:");
		JTextField ip = new JTextField(16);
		JButton button=new JButton("Join");//creating instance of JButton  
		
		method_panel.add(label);
		method_panel.add(ip);
		method_panel.add(button);
		f.add(method_panel);
	
	}
	
	public static void spawn_host_info() throws IOException {
		byte[] ip = InetAddress.getLocalHost().getAddress();
		method_panel = new JPanel();
		JLabel label = new JLabel("IP: "+ InetAddress.getByAddress(ip).toString().substring(1));
		JButton button=new JButton("Start Game");//creating instance of JButton  
		
		method_panel.add(label);
		method_panel.add(button);
		f.add(method_panel);
	
	}
	
	public static void spawn_connection_selector() throws IOException {
		String[] connectiontypes = {"Host","Select IP","Enter address manually"}; 
		JPanel panel = new JPanel();
		JComboBox method_selector = new JComboBox(connectiontypes);
		JLabel label = new JLabel("Select your connection type:");
		panel.add(label);
		panel.add(method_selector);
		f.add(panel);
		
		//spawn initial option
		spawn_host_info();
		method_selector.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	f.remove(method_panel);
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
		    	
		    	SwingUtilities.updateComponentTreeUI(f);
		    }
		});
	
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
		
	}
}