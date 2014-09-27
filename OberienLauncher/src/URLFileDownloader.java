import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class URLFileDownloader {
	private URL url;
	private File file;

	public URLFileDownloader(String s) throws MalformedURLException {
		this.url = new URL("http://oberien.bplaced.net/Oberien/" + s);
		s = s.replace("%20", " ");
		if (s.contains("natives")) {
			s = "Game/" + s.substring(s.lastIndexOf("/"));
		}
		file = new File(s);
	}

	public void setOutputFile(String s) {
		file = new File(s.replace("%20", " "));
	}

	public void download() throws IOException {
		if (!file.exists()) {
			File temp = new File(file.toString().substring(0,
					file.toString().lastIndexOf("\\") + 1));
			temp.mkdirs();
			file.createNewFile();
		}
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(file));
		byte[] buffer = new byte[1024];
		int len;
		while ((len = bis.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bis.close();
		bos.close();
	}
}
