package com.rj.soundcloudlivewallpaper;

import java.util.Arrays;

import android.graphics.Bitmap;
import android.util.Log;

public class WaveformProcessor {

	
	public static float[] getWaveformFromBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		Log.d("WaveformProcessor", "width "+width);
		float[] vals = new float[width];
		Arrays.fill(vals, -1f);
		float height = bitmap.getHeight()/2;
		int middle = bitmap.getHeight()/2;
		long start = System.currentTimeMillis();
		for (int i=0; i<width; i++) {
			for (int j=0; j<middle && vals[i] == -1f; j++) {
				int alpha = bitmap.getPixel(i, j) >>> 24;

				if (alpha <= 0) {
					vals[i] = (float)j/height;
				}
			}
		}
		long end = System.currentTimeMillis();
		Log.d("WaveformProcessor", "total time: "+(end-start));
		return vals;
	}
}
