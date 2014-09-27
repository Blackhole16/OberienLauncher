import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

class UpdateThread extends Thread {
	private Launcher launcher;
	private String[] versions;
	private String clientVersion;
	private boolean finished = false;
	
	public UpdateThread(Launcher launcher, String[] versions, String clientVersion) {
		this.launcher = launcher;
		this.versions = versions;
		this.clientVersion = clientVersion;
	}
	
	public void run() {
		//get all files to download and to remove
		ArrayList<String> allFileNames = new ArrayList<String>();
		ArrayList<String> allRemoveFileNames = new ArrayList<String>();
		try {
			String[] fileNames;
			for (int i = 0; i < versions.length && !clientVersion.equals(versions[i]); i++) {
				fileNames = OberienURL.getFileNames(versions[i]);
				for (String s : fileNames) {
					if (!allFileNames.contains(s)) {
						allFileNames.add(s);
					}
				}
				fileNames = OberienURL.getRemoveFileNames(versions[i]);
				for (String s : fileNames) {
					if (!allRemoveFileNames.contains(s)) {
						allRemoveFileNames.add(s);
					}
				}
			}
			//remove
			String currentFileName;
			for (int i = 0; i < allRemoveFileNames.size(); i++) {
				currentFileName = allRemoveFileNames.get(i);
				if (currentFileName.endsWith("/")) {
					launcher.append("Deleting Folder '" + currentFileName + "'");
				} else {
					launcher.append("Deleting '" + currentFileName + "'");
				}
				new File(currentFileName).delete();
			}
			
			//download
			for (int i = 0; i < allFileNames.size(); i++) {
				currentFileName = allFileNames.get(i);
				if (currentFileName.endsWith("/")) {
					launcher.append("Creating folder '" + currentFileName + "'");
					new File(currentFileName).mkdir();
				} else {
					launcher.append("Downloading '" + currentFileName + "'");
					new URLFileDownloader(currentFileName).download();
				}
			}
			launcher.saveVersion();
			launcher.updateVersion();
			
		} catch (IOException e) {e.printStackTrace(); JOptionPane.showMessageDialog(null, e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
		} catch (StringIndexOutOfBoundsException e) {e.printStackTrace(); JOptionPane.showMessageDialog(null, e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);}
		finished = true;
	}
	
	public boolean isFinished() {
		return finished;
	}
}