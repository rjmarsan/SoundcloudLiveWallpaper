package com.rj.soundcloudlivewallpaper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class SoundcloudApi {

	private final static String API_KEY = "fb37129b756c004bc67d51933f29c4ae";
	private final static String API_ENDPOINT = "http://api.soundcloud.com";
	private final static String TRACKS_URL = "/tracks/%s.json?client_id=%s";
	
	
	private static JSONObject getTrack(int id) throws IOException, JSONException {
		return get(API_ENDPOINT + String.format(TRACKS_URL, id+"", API_KEY));
	}
	
	private static JSONObject get(String stringurl) throws IOException, JSONException {
		URL url = new URL(stringurl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try {
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String out = stringFromInputStream(in);
			JSONObject json = new JSONObject(out);
			return json;
		} finally {
			urlConnection.disconnect();
		}
	}
	
	private static String stringFromInputStream(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();
        return sb.toString();
	}

}
