package com.rj.soundcloudlivewallpaper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.rj.soundcloudlivewallpaper.api.Track;

public class SoundcloudApi {

	private final static String API_KEY = "fb37129b756c004bc67d51933f29c4ae";
	private final static String API_ENDPOINT = "http://api.soundcloud.com";
	private final static String SOUNDCLOUD_URL = "http://soundcloud.com";
	private final static String TRACKS_URL = "/tracks/%s.json?client_id=%s";
	private final static String USERS_TRACKS_URL = "/users/%s/tracks.json?client_id=%s";
	
	
	private final static String RESOLVE_URL = "/resolve.json?url=%s&client_id=%s";
//	private final static String RESOLVE_RESULT_KEY = "result";
//	private final static String RESOLVE_RESULT_REDIRECT = "302 - Found";
	private final static String RESOLVE_ID = "id";
//	private final static String RESOLVE_LOCATION = "location";

	
	
	private static JSONArray getTracksFromUser(String userid) throws IOException, JSONException {
		return getArray(API_ENDPOINT + String.format(USERS_TRACKS_URL, userid, API_KEY));
	}
	
	private static String getUserIdForName(String name) throws IOException, JSONException {
		JSONObject result = getObject(API_ENDPOINT + String.format(RESOLVE_URL, SOUNDCLOUD_URL+"/"+name, API_KEY));
//		if (RESOLVE_RESULT_REDIRECT.equals(result.getString(RESOLVE_RESULT_KEY))) {
//			return result.getString(RESOLVE_LOCATION);
//		} else {
//			return result.getString()
//		}
		return result.getString(RESOLVE_ID);
		//otherwise
//		return null;
	}
	
	private static JSONObject getTrack(int id) throws IOException, JSONException {
		return getObject(API_ENDPOINT + String.format(TRACKS_URL, id+"", API_KEY));
	}

	public static String getWaveformUrlForTrack(int id) throws IOException, JSONException {
		JSONObject track = getTrack(id);
		return new Track(track).waveformUrl;
	}
	
	public static List<Track> getTracksForUserFavorites(String name) throws IOException, JSONException {
		String userid = getUserIdForName(name);
		JSONArray tracks = getTracksFromUser(userid);
		List<Track> outlist = new ArrayList<Track>();
		for (int i=0; i<tracks.length(); i++) {
			outlist.add(new Track(tracks.getJSONObject(i)));
		}
		return outlist;
	}

	/* some ways to get tracks */

	
	
	
	/* *********************** */
	
	private static String get(String stringurl) throws IOException, JSONException {
		URL url = new URL(stringurl);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		try {
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			String out = stringFromInputStream(in);
			Log.d("SoundcloudAPI", "Result for "+stringurl+": "+out);
			return out;
		} finally {
			urlConnection.disconnect();
		}
	}

	private static JSONObject getObject(String stringurl) throws IOException, JSONException {
		String out = get(stringurl);
		return new JSONObject(out);
	}
	private static JSONArray getArray(String stringurl) throws IOException, JSONException {
		String out = get(stringurl);
		return new JSONArray(out);
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
