package com.rj.soundcloudlivewallpaper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class BackgroundManager {
	
	Context mContext;
	WaveformDrawer drawer;
	Runnable requestDrawRunnable;
	long timeOfLastRequest;
	long timeBetweenForcedUpdates = 5*1000;
	
	Handler requestHandler = new Handler();
	
	public BackgroundManager(Context context) {
		this.mContext = context;
		drawer = createWaveformDrawer();
	}
	
	
    private WaveformDrawer createWaveformDrawer() {
    	WaveformDrawer drawer = new WaveformDrawer(mContext);
    	//drawer.setWaveform(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.demowaveform));
    	
    	return drawer;
    }
    
    public void setRequestDrawRunnable(Runnable runnable) {
    	this.requestDrawRunnable = runnable;
    }
    
    private void requestDraw() {
    	if (this.requestDrawRunnable != null) 
    		requestDrawRunnable.run();
    }
    
    public void resize(int width, int height) {
    	drawer.resize(width, height);
    }

	/**
	 * Starts the process of downloading images, etc. can be called many times.
	 */
	public void start() {
		requestUpdate();
	}
	
	/**
	 * Stops the process of downloading images, etc. can be called many times.
	 */
	public void stop() {
		
	}
	
	public void cleanup() {
		drawer.cleanup();
	}
	
	
	public void draw(Canvas c, float mOffset) {
		drawer.draw(c, mOffset);
	}
	
	
	public void requestUpdate() {
		long now = System.currentTimeMillis();
		if (now - timeOfLastRequest < timeBetweenForcedUpdates) return; //wait until later.
		Log.d("BackgroundManager", "Updating now");
		timeOfLastRequest = now;
		new RandomWaveformTask().execute();
	}
	
	public void scheduleUpdate() {
		requestHandler.postDelayed(new Runnable() {
			public void run() {
				requestUpdate();
			}
		}, timeBetweenForcedUpdates);
	}
	
	
	private final static String[] usernames = {
		"ableton"
	};
	public class RandomWaveformTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... params) {
			String username = usernames[0];
			try {
				List<String> waveforms = SoundcloudApi.getWaveformUrlsForUserFavorites(username);
				setBitmap(pickRandomFromList(waveforms));
			} catch (Exception e) {
				e.printStackTrace();
			} 
			scheduleUpdate();
			return null;
		}
	}
	
	private String pickRandomFromList(List<String> list) {
		Random r = new Random();
		return list.get(r.nextInt(list.size()));
	}
	
	public Bitmap fetchBitmap(String url) throws MalformedURLException, IOException {
		Log.d("BackgroundManager", "Fettching url: "+url);
		InputStream is = (InputStream) new URL(url).getContent();
		return BitmapFactory.decodeStream(is);
	}
	
	public void setBitmap(String url) throws MalformedURLException, IOException {
		Bitmap bitmap = fetchBitmap(url);
		drawer.setWaveform(bitmap);
		requestDraw();
	}

}
