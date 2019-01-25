package com.koscom.burning.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class HttpClientUtil {

	public String execute(String urlstring) {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlstring);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			
			byte [] data;
			if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
				data = StreamUtils.copyToByteArray(conn.getInputStream());
			} else {
				data = StreamUtils.copyToByteArray(conn.getErrorStream());
			}
			
			return new String(data, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null)
				conn.disconnect();
		}
			
	}
}
