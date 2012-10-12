package com.rj.soundcloudlivewallpaper.api;

import org.json.JSONException;
import org.json.JSONObject;

public class Track {
	public final static String WAVEFORM_URL_KEY = "waveform_url";
	public final static String PERMALINK_URL_KEY = "permalink_url";

	public JSONObject backingJson;
	public String waveformUrl;
	public String permalinkUrl;
	
	public Track(JSONObject backingJson) throws JSONException {
		this.backingJson = backingJson;
		waveformUrl = backingJson.getString(WAVEFORM_URL_KEY);
		permalinkUrl = backingJson.getString(PERMALINK_URL_KEY);
	}
	public Track(String waveformurl, String permalink) {
		this.waveformUrl = waveformurl;
		this.permalinkUrl = permalink;
	}
}
