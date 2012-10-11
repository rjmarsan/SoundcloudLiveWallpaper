package com.rj.soundcloudlivewallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class WaveformDrawer {

	Bitmap waveform;
	Bitmap processedWaveform;
	Context mContext;
	Paint paint;
	int width = -1;
	int height = -1;
	
	public WaveformDrawer(Context context) {
		this.mContext = context;
		paint = new Paint();
	}
	
	public void cleanup() {
		waveform.recycle();
		waveform = null;
		processedWaveform.recycle();
		processedWaveform = null;
	}
	
	public void setWaveform(String path) {
		setWaveform(BitmapFactory.decodeFile(path));
	}
	public void setWaveform(Bitmap bitmap) {
		setAndPreprocessBitmap(bitmap);
	}
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		if (waveform != null) setWaveform(waveform);
	}
	
	
	/**
	 * Doing effects, etc, that we don't need to do every draw()
	 * @param bitmap
	 */
	private void setAndPreprocessBitmap(Bitmap bitmap) {
		if (bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) return;
		waveform = bitmap;
		Bitmap resize = resizeBitmap(bitmap);
		Bitmap blurry = makeBlurry(resize, 10);
		
		
		processedWaveform = combineImages(resize,blurry);
		resize.recycle();
		blurry.recycle();
	}
	
	private Bitmap resizeBitmap(Bitmap bitmap) {
		if (width < -1 || height < -1) return bitmap; //we don't know our sizes yet.
		
		float aspectratio = (float)bitmap.getWidth() / (float)bitmap.getHeight();
		int calculatedwidth = (int)(height * aspectratio);
		int amountOfWidthWeWant = width * 2;
		float usedWidthRatio = (float)amountOfWidthWeWant/(float)calculatedwidth;

		int usedWidthOfOriginal = (int)(bitmap.getWidth()*usedWidthRatio);
		Bitmap clippedbitmap = Bitmap.createBitmap(bitmap, 0, 0, usedWidthOfOriginal, bitmap.getHeight());
		applyAlphaToBitmap(clippedbitmap, 0x8F000000);
		
		Log.d("WaveformDrawer", "Our corrected width: "+amountOfWidthWeWant + " and our aspect ratio: "+aspectratio);
		return Bitmap.createScaledBitmap(clippedbitmap, amountOfWidthWeWant, height, false);
	}
	
	private Bitmap makeBlurry(Bitmap bitmap, int blurry) {
		Bitmap small = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/blurry, bitmap.getHeight()/blurry, true);
		return Bitmap.createScaledBitmap(small, bitmap.getWidth(), bitmap.getHeight(), true);
	}
	
	private void applyAlphaToBitmap(Bitmap bitmap, int alphamask) {
		for(int i = 0; i < bitmap.getWidth(); i++) {
		    for(int j = 0; j < bitmap.getHeight(); j++) {
		    	int color = bitmap.getPixel(i,j);
		    	color = color + (((color & 0xFF000000) == 0xFF000000) ? alphamask : 0); //don't clobber 100% transparent
		         bitmap.setPixel(i,j,color);
		    }
		}
	}

	public Bitmap combineImages(Bitmap c, Bitmap s) {
		Bitmap cs = null;

		int width, height = 0;
		
		width = c.getWidth();
		height = c.getHeight() + s.getHeight();

		cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas comboImage = new Canvas(cs);

		comboImage.drawBitmap(c, 0f, 0f, null);
		comboImage.drawBitmap(s, 0f, 0f, null);

		return cs;
	}
	
	public void draw(Canvas c, float mOffset) {
		
		
        c.save();
        c.drawColor(0xfff8350e);
        if (waveform != null) {
        	float offset = mOffset*-(processedWaveform.getWidth()-c.getClipBounds().width());
        	c.drawBitmap(processedWaveform, offset, 0, paint);
		
        }
        c.restore();

	}
	
	
}
