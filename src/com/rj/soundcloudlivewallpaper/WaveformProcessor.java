package com.rj.soundcloudlivewallpaper;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * no real reason this has to be a separate class, I just really wanted all of this
 * ugly ugly code as far away from my other stuff as possible.
 * @author rj
 *
 */
public class WaveformProcessor {
	public final static String TAG = "WaveformProcessor";
	
	/**
	 * walks through every column of the image and finds the first transparent pixel. 
	 * we assume that that's the beginning of the waveform, so we mark it's relative distance down.
	 * @param bitmap
	 * @return
	 */
	public static float[] getWaveformFromBitmap(Bitmap bitmap) {
		final int width = bitmap.getWidth();
		Log.d(TAG, "width "+width);
		final float[] vals = new float[width];
		Arrays.fill(vals, -1f);
		final float height = bitmap.getHeight()/2;
		final int middle = bitmap.getHeight()/2;
		final long start = System.currentTimeMillis();
		for (int i=0; i<width; i++) {
			for (int j=0; j<middle && vals[i] == -1f; j++) {
				int alpha = bitmap.getPixel(i, j) >>> 24;

				if (alpha <= 0) {
					vals[i] = (float)j/height;
				}
			}
		}
		long end = System.currentTimeMillis();
		Log.d(TAG, "total time: "+(end-start)); //yeah, I'm concerned how long this takes
		return vals;
	}
}
