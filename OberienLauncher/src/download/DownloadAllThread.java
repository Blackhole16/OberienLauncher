package download;

import view.Launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class DownloadAllThread extends Thread {
	private Launcher launcher;
	private boolean finished = false;
	
	public DownloadAllThread(Launcher launcher) {
		this.launcher = launcher;
	}
	
	public void run() {
		new File("Game/").mkdir();
		try {
			String[] files = getFiles("Game/");
			launcher.setFileNumber(files.length);
			for (String s : files) {
				if (s.endsWith("/")) {
					launcher.append("Creating folder '" + s + "'");
					new File(s).mkdir();
				} else {
					launcher.append("Downloading '" + s + "'");
					new URLFileDownloader(s).download();
				}
			}
			launcher.saveVersion();
			launcher.updateVersion();
		} catch (IOException e) {e.printStackTrace(); JOptionPane.showMessageDialog(null, e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);}
		finished = true;
	}
	
	private String[] getFiles(String s) throws IOException {
		if (s.equals("Game/natives/")) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				s = "Game/natives/windows/";
			} else if (os.contains("mac")) {
				s = "Game/natives/macosx/";
			} else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
				s = "Game/natives/linux";
			} else if (os.contains("sunos/")) {
				s = "Game/natives/solaris";
			} else {
				s = "Game/natives/freebsd/";
			}
		}
		ArrayList<String> files = new ArrayList<String>();
		String page = OberienURL.get(s);
		page = page.substring(page.indexOf("Parent Directory</a>")+20);
		int from = page.indexOf("<img");
		int to = page.indexOf("</table>");
		if (from < 0 || to < 0) {
			return new String[0];
		}
		page = page.substring(from, to);
		String[] lines = page.split("\n");
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].substring(lines[i].indexOf("<a href=\"")+9);
			lines[i] = s + lines[i].substring(0, lines[i].indexOf("\">"));
			files.add(lines[i]);
			if (lines[i].endsWith("/")) {
				String[] sub = getFiles(lines[i]);
				for (int j = 0; j < sub.length; j++) {
					files.add(sub[j]);
				}
			}
		}
		
		String[] ret = new String[files.size()];
		ret = files.toArray(ret);
		return ret;
	}
	
	public boolean isFinished() {
		return finished;
	}
}