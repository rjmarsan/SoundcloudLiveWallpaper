package com.rj.soundcloudlivewallpaper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.rj.soundcloudlivewallpaper.api.SoundcloudApi;
import com.rj.soundcloudlivewallpaper.api.Track;

/**
 * Essentially the main controller for the wallpaper, it puts all the components together, and does quite a bit itself.
 * @author rj
 *
 */
public class BackgroundManager {
	public final static String TAG = "BackgroundManager";
	
	Context mContext;
	WaveformDrawer drawer;
	
	long timeOfLastRequest;
	long timeBetweenForcedUpdatesWIFI = 15*1000L;
	long timeBetweenForcedUpdates3G = 30*1000L;
	long timeBetweenForcedUpdates = timeBetweenForcedUpdatesWIFI;
	
	long durationArtist = 15*60*1000L;
	
	long timeSinceArtistUpdate;
	List<Track> selectedTracks;
	String selectedArtist;
	Track selectedTrack;
	
	
	Handler requestHandler = new Handler();
	boolean stopped = false;
	
	public BackgroundManager(Context context) {
		this.mContext = context;
		drawer = createWaveformDrawer();
	}
	
	
    private WaveformDrawer createWaveformDrawer() {
    	WaveformDrawer drawer = new WaveformDrawer(mContext);
    	return drawer;
    }
    
    public void resize(int width, int height) {
    	drawer.resize(width, height);
    }

	/**
	 * Starts the process of downloading images, etc. can be called many times.
	 */
	public void start() {
		stopped = false;
		requestUpdate();
	}
	
	/**
	 * Stops the process of downloading images, etc. can be called many times.
	 */
	public void stop() {
		stopped = true;
	}
	
	public void cleanup() {
		drawer.cleanup();
	}
	
	
	public void draw(Canvas c, float mOffset) {
		drawer.draw(c, mOffset);
	}
	
	
	public void clicked() {
		Log.d(TAG, "Clicked! ");
		//if (1 == 1) return; //short circuit FTw
		if (selectedTrack != null && selectedTrack.permalinkUrl != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW);                  
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(android.net.Uri.parse(selectedTrack.permalinkUrl));
			mContext.startActivity(intent);
		}
	}
	
	public void requestUpdate() {
		if (stopped) return; //no reason at all.
		long now = System.currentTimeMillis();
		if (now - timeOfLastRequest < timeBetweenForcedUpdates) return; //wait until later.
		Log.d(TAG, "Updating now "+timeBetweenForcedUpdates);
		timeOfLastRequest = now;
		new RandomWaveformTask().execute();
		checkNetworkState();
	}
	
	public void scheduleUpdate() {
		requestHandler.postDelayed(new Runnable() {
			public void run() {
				requestUpdate();
			}
		}, timeBetweenForcedUpdates);
	}
	
	public void checkNetworkState() {
		try {
			ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
			boolean isMobile = networkInfo.isConnected();
			if (isMobile) {
				timeBetweenForcedUpdates = timeBetweenForcedUpdates3G;
			} else {
				timeBetweenForcedUpdates = timeBetweenForcedUpdatesWIFI;
			}
		} catch (Exception e) {
			//we're probably on wifi and this is a tablet.
			timeBetweenForcedUpdates = timeBetweenForcedUpdatesWIFI;
		}

	}
	
	
	//TODO: make this a more interesting selection system
	private final static String[] artists = {
		"ableton", 
		"indigolab", 
		"franceinter", 
		"jagwar-ma", 
		"hunter-hoburg", 
		"max-richter", 
		"siansanderson", 
		"viciousminuteshour", 
		"glacierface", 
		"brian-hoffer", 
		"tedxglobalmusicproject", 
		"blondes"
	};
	public class RandomWaveformTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... params) {
			String artist = artists[0];
			try {
				//try to download album art.
				List<Track> tracks = selectedTracks;
				if (tracks == null || timeToGetNewArtist()) {
					tracks = SoundcloudApi.getTracksForUserFavorites(artist);
				}
				Track track = pickRandomFromList(tracks);
				selectTrack(track, tracks, artist);
			} catch (Exception e) {
				e.printStackTrace();
				//well, let's see if we can grab one from the cache dir.
				File[] files = getCacheDir().listFiles();
				File file;
				if (files.length > 0) {
					file = files[new Random().nextInt(files.length)];
				} else {
					file = new File(getCacheDir(), "default.png");
					try {
						Utils.copyFile(mContext.getAssets().open("default.png"), new FileOutputStream(file));
					} catch (Exception e1) {
						//well this is bad.
						e1.printStackTrace();
					} 
				}
				try {
					selectTrack(new Track(Uri.fromFile(file).toString(), null),selectedTracks,selectedArtist);
				} catch (IOException e1) {
					e1.printStackTrace();
					//well... now there's Nothing we can do.
				}
			} 
			scheduleUpdate();
			return null;
		}
	}
	
	private void selectTrack(Track track, List<Track> tracks, String artist) throws IOException {
		setBitmap(track.waveformUrl);
		
		this.selectedTrack = track;
		this.selectedTracks = tracks;
		if (selectedArtist == null || artist.equals(this.selectedArtist) == false) {
			this.timeSinceArtistUpdate = System.currentTimeMillis();
		}
		this.selectedArtist = artist;
		
	}
	
	private boolean timeToGetNewArtist() {
		return System.currentTimeMillis() - this.timeSinceArtistUpdate > this.durationArtist;
	}
	
	private <T> T pickRandomFromList(List<T> list) {
		Random r = new Random();
		return list.get(r.nextInt(list.size()));
	}
	
	public Bitmap fetchBitmap(String url) throws MalformedURLException, IOException {
		Log.d(TAG, "Fettching url: "+url);
		InputStream is = (InputStream) new URL(url).getContent();
		return BitmapFactory.decodeStream(is);
	}
	
	public void setBitmap(String url) throws MalformedURLException, IOException {
		Bitmap bitmap = fetchBitmap(url);
		drawer.setWaveform(bitmap);
		saveBitmap(url, bitmap);
	}
	
	private void saveBitmap(String url, Bitmap bitmap) {
		try {
			File outfile = new File(getCacheDir(), url.hashCode()+".png");
			Log.d(TAG, "Saving file to "+outfile);
			FileOutputStream out = new FileOutputStream(outfile);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			trimCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private File getCacheDir() {
		return mContext.getCacheDir();
	}

	private void trimCache() {
		File cache = getCacheDir();
		int max_size = 1000 * 1000; //1MB
		int size = 0;
	    //clear SD cache
	    File[] files = cache.listFiles();
	    for (File f:files) {
	        size += f.length();
	        if (size > max_size) {
	        	Log.d(TAG, "Removing item from cache: "+f);
	        	f.delete();
	        }
	    }
	}
}
