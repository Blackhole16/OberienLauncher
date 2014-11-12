package download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class OberienURL {

	public static String get(String s) throws IOException {
		URL url = new URL("http://oberien.net/download/Oberien/" + s);
		InputStream is = url.openStream();
		int i;
		StringBuffer sb = new StringBuffer();
		while ((i = is.read()) != -1) {
			char c = (char) i;
			sb.append(c);
		}
		is.close();
		return sb.toString();
	}

	public static String getChanges() throws IOException {
		return get("announcements.txt");
	}

	public static String[] getVersions() throws IOException {
		String version = get("version.txt");
		String[] versions = version.split("\n");
		for (int i = 0; i < versions.length; i++) {
			versions[i] = versions[i].substring(
					version.indexOf("[version]") + 9,
					version.indexOf("[/version]"));
		}
		return versions;
	}
	
	public static String[] getFileNames(String version) throws IOException {
		String s = OberienURL.get(version);
		String[] fileNames = s.substring(s.indexOf("[files]")+7, s.indexOf("[/files]")).split("\n");
		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = "Game/" + fileNames[i];
		}
		return fileNames;
	}
	
	public static String[] getRemoveFileNames(String version) throws IOException {
		String s = OberienURL.get(version);
		String[] fileNames = s.substring(s.indexOf("[remove]")+7, s.indexOf("[/remove]")).split("\n");
		for (int i = 0; i < fileNames.length; i++) {
			fileNames[i] = "Game/" + fileNames[i];
		}
		return fileNames;
	}
	
	public static String getHistory(String version) throws IOException {
		String file = get(version);
		file = file.substring(file.indexOf("[history]") + 9,
				file.indexOf("[/history]"));
		return file;
	}
}
