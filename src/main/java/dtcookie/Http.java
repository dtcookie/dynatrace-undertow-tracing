package dtcookie;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.TimerTask;

public final class Http extends TimerTask {
	
	private final String url;
	
	public Http(String url) {
		Objects.requireNonNull(url);
		this.url = url;
	}
	
	@Override
	public void run() {
		try {
			try (InputStream in = new URL(url).openStream()) {
				byte[] buffer = new byte[4096];
				int len = in.read(buffer);
				while (len > 0 ) {
					len = in.read(buffer);
				}
			}
		} catch (Throwable t) {
			// ignore
		}
	}
	
}
