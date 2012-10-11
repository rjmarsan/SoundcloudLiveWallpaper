package com.rj.soundcloudlivewallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

public class WaveformDrawer {

	Bitmap waveform;
	Context mContext;
	Paint paint;
	
	public WaveformDrawer(Context context) {
		this.mContext = context;
		paint = new Paint();
	}
	
	public void cleanup() {
		
	}
	
	public void setWaveform(String path) {
		waveform = BitmapFactory.decodeFile(path);
	}
	public void setWaveform(Bitmap bitmap) {
		waveform = bitmap;
	}
	
	public void draw(Canvas c, float mOffset) {
		
		
        c.save();
        c.drawColor(0xff00f000);
        if (waveform != null) {
        	c.drawBitmap(waveform, 0, 0, paint);
		
		
        }
        c.restore();

	}
	
	
}
