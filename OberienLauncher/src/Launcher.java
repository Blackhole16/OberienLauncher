import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;


public class Launcher extends JFrame implements ActionListener {
	private boolean update;
	private String[] versions;
	private String serverVersion;
	private String clientVersion;
	
	private JTextArea status;
	private JScrollBar vertical;
	private JLabel versionLabel;
	private JButton updateButton;
	
	private int fileNumber;
	private int currentFile;
	
	private DownloadAllThread downloadAllThread;
	private UpdateThread updateThread;
	
	public Launcher() throws IOException {
		super("Oberien Launcher");
		versions = OberienURL.getVersions();
		
		JMenuBar mb = new JMenuBar();
		JMenu options = new JMenu("Options");
		JMenuItem redownload = new JMenuItem("Redownload whole game");
		redownload.addActionListener(this);
		redownload.setActionCommand("redownload");
		options.add(redownload);
		mb.add(options);
		
		JMenu changelog = new JMenu("Changelog");
		JMenuItem[] changelogItem = new JMenuItem[versions.length];
		for (int i = 0; i < changelogItem.length; i++) {
			changelogItem[i] = new JMenuItem(versions[i]);
			changelog.add(changelogItem[i]);
			changelogItem[i].addActionListener(this);
			changelogItem[i].setActionCommand("version:"+versions[i]);
		}
		mb.add(changelog);
		
		setJMenuBar(mb);
		
		JLabel north = new JLabel("Oberien");
		north.setFont(new Font("Arial", Font.BOLD, 32));
		
		JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		JLabel changes = new JLabel("Announcements");
		changes.setFont(new Font("Arial", Font.PLAIN, 20));
		center.add(changes);
		
		JTextPane changesPane = new JTextPane();
		changesPane.setEditable(false);
		changesPane.setContentType("text/html");
		changesPane.setText(OberienURL.getChanges());
		center.add(changesPane);
		
		JLabel statusL = new JLabel("Status");
		statusL.setFont(new Font("Arial", Font.PLAIN, 20));
		center.add(statusL);
		
		status = new JTextArea();
		status.setEditable(false);
		JScrollPane sp = new JScrollPane(status);
		vertical = sp.getVerticalScrollBar();
		center.add(sp);
		
		JPanel south = new JPanel();
		south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
		status.append("Getting server version...\n");
		serverVersion = versions[0];
		
		status.append("Getting local version...\n");
		try {
			clientVersion = getClientVersion();
		} catch (FileNotFoundException e) {
			clientVersion = "No version found";
		}
		
		status.append("Server version: " + serverVersion + "\n");
		status.append("Local version:  " + clientVersion + "\n");
		
		update = !clientVersion.equals(serverVersion);
		versionLabel = new JLabel("Client: " + clientVersion + ";  Server: " + serverVersion + ";  " + (update ? "Update needed.   " : "Up to date.   "));
		if (update) {
			status.append("Update needed\n");
			versionLabel.setForeground(Color.RED);
		} else {
			status.append("Up to date\n");
			versionLabel.setForeground(new Color(0,154,0));
		}
		south.add(versionLabel);
		
		if (update) {
			updateButton = new JButton("Update");
			updateButton.addActionListener(this);
			south.add(updateButton);
		}
		
		JButton start = new JButton("Start");
		start.addActionListener(this);
		south.add(start);
		
		
		add(north, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void updateVersion() {
		fileNumber = 0;
		currentFile = 0;
		try {
			versions = OberienURL.getVersions();
			status.append("Update finished\n\n");
			status.append("Reinitialize verion label...\n");
			serverVersion = versions[0];
			
			status.append("Getting local version...\n");
			try {
				clientVersion = getClientVersion();
			} catch (FileNotFoundException e) {
				clientVersion = "No version found";
			}
			
			status.append("Server version: " + serverVersion + "\n");
			status.append("Local version:  " + clientVersion + "\n");
			update = !clientVersion.equals(serverVersion);
			versionLabel.setText("Client: " + clientVersion + ";  Server: " + serverVersion + ";  " + (update ? "Update needed.   " : "Up to date.   "));
			if (update) {
				status.append("Update needed\n");
				versionLabel.setForeground(Color.RED);
			} else {
				status.append("Up to date\n");
				versionLabel.setForeground(new Color(0,154,0));
				updateButton.setVisible(false);
			}
			vertical.setValue(vertical.getMaximum());
		} catch (IOException e1) {e1.printStackTrace();}
	}
	
	private String getClientVersion() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("version.txt"));
		String s = br.readLine();
		br.close();
		return s;
	}
	
	public void saveVersion() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("version.txt"));
		bw.write(serverVersion);
		bw.close();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (ac.equals("Update")) {
			status.append("Update starting...");
			currentFile = 0;
			if (clientVersion.equals("No version found")) {
				if (downloadAllThread == null || downloadAllThread.isFinished()) {
					status.append("No version found. Downloading everything...");
					downloadAllThread = new DownloadAllThread(this);
					downloadAllThread.start();
				} else {
					status.append("ERROR: The current download hasn't finished yet.");
					JOptionPane.showMessageDialog(null, "The current download hasn't finished yet.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				if (updateThread == null || updateThread.isFinished()) {
					status.append("Performing update...");
					updateThread = new UpdateThread(this, versions, clientVersion);
					updateThread.start();
				} else {
					status.append("ERROR: The current download hasn't finished yet.");
					JOptionPane.showMessageDialog(null, "The current download hasn't finished yet.", "ERROR", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (ac.equals("Start")) {
			if (fileNumber != currentFile) {
				JOptionPane.showMessageDialog(null, "Your client isn't updated yet. Please wait.", "ERROR", JOptionPane.ERROR_MESSAGE);
			}
			if (update) {
				int selection = JOptionPane.showConfirmDialog(null, "Your game is not up to date! Start anyways?", "Not up to date", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (selection == 0) {
					start();
				}
			} else {
				start();
			}
		} else if (ac.equals("redownload")) {
			currentFile = 0;
			new DownloadAllThread(this).start();
		} else if (ac.startsWith("version:")) {
			String version = ac.substring(8);
			String history = null;
			try {
				history = OberienURL.getHistory(version);
			} catch (IOException e1) {e1.printStackTrace();}
			JTextPane tp = new JTextPane();
			tp.setEditable(false);
			tp.setContentType("text/html");
			tp.setText(history);
			
			JDialog d = new JDialog(this, "History of " + version, false);
			d.add(tp);
			d.pack();
			d.setLocationRelativeTo(this);
			d.setVisible(true);
		}
	}
	
	private void start() {
		try {
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "Oberien.jar");
			pb.directory(new File("Game/"));
			pb.start();
		} catch (IOException e) {e.printStackTrace();}
		System.exit(0);
	}
	
	public void setFileNumber(int i) {
		fileNumber = i;
	}
	
	public void append(String s) {
		currentFile++;
		status.append(s + " (" + currentFile + "/" + fileNumber + ")\n");
		vertical.setValue(vertical.getMaximum());
	}
}
