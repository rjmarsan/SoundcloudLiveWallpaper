package com.rj.soundcloudlivewallpaper;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class WaveformDrawer {

	Bitmap waveform;
	Bitmap processedWaveform;
	Bitmap oldProcessedWaveform;
	ColorsMap map = new ColorsMap(ColorsMap.DEFAULT_MAP, 7000);
	Context mContext;
	Paint paint;
	Paint displaypaint;
	int width = -1;
	int height = -1;
	long timeSinceTransition;
	long transitionTime = 2*1000L;
	
	public WaveformDrawer(Context context) {
		this.mContext = context;
		paint = new Paint();
		displaypaint = new Paint();
	}
	
	public void cleanup() {
		waveform.recycle();
		waveform = null;
		processedWaveform.recycle();
		processedWaveform = null;
		oldProcessedWaveform.recycle();
		oldProcessedWaveform = null;
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
		//if (waveform != null && waveform != bitmap) waveform.recycle();
		waveform = bitmap;
		
		int scaledWidth = width*2;
		int scaledHeight = height; //no change here.
		int clippedWidth = getScaledWidth(scaledWidth, bitmap);
		Bitmap clipped = clipBitmap(bitmap, clippedWidth);
		
		Bitmap canvasbmp = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(canvasbmp);
		Rect canvasrect = canvas.getClipBounds();
		Rect clippedrect = new Rect(0, 0, clipped.getWidth(), clipped.getHeight());
		
		paint.setFilterBitmap(true);
		paint.setAlpha(120);
		canvas.drawBitmap(makeBlurry(bitmap, 3), clippedrect, canvasrect, paint);
		paint.setFilterBitmap(false);
		paint.setAlpha(180);
		canvas.drawBitmap(bitmap, clippedrect, canvasrect, paint);

		
		oldProcessedWaveform = processedWaveform;
		processedWaveform = canvasbmp;
		timeSinceTransition = System.currentTimeMillis();
		//if (oldwaveform != null) oldwaveform.recycle();
		clipped.recycle();
	}
	
	private int getScaledWidth(int targetwidth, Bitmap reference) {
		float aspectratio = (float)reference.getWidth() / (float)reference.getHeight();
		int calculatedwidth = (int)(height * aspectratio);
		int amountOfWidthWeWant = targetwidth;
		float usedWidthRatio = (float)amountOfWidthWeWant/(float)calculatedwidth;

		int usedWidthOfOriginal = (int)(reference.getWidth()*usedWidthRatio);
		return usedWidthOfOriginal;
	}
	
	private Bitmap clipBitmap(Bitmap bitmap, int clipWidth) {
		float offset = new Random().nextFloat();
		float left = offset*(bitmap.getWidth()-clipWidth);
		Bitmap clippedbitmap = Bitmap.createBitmap(bitmap, (int)left, 0, clipWidth, bitmap.getHeight());
		return clippedbitmap;
	}
	
	private Bitmap makeBlurry(Bitmap bitmap, int blurry) {
		Bitmap small = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/blurry, bitmap.getHeight()/blurry, true);
		return Bitmap.createScaledBitmap(small, bitmap.getWidth(), bitmap.getHeight(), true);
	}
	

	
	public void draw(Canvas c, float mOffset) {
		
		
        c.save();
        c.drawColor(map.getColor());
        if (processedWaveform != null) {
        	float offset = mOffset*-(processedWaveform.getWidth()-c.getClipBounds().width());
        	
        	long time = System.currentTimeMillis();
        	if (time - timeSinceTransition < transitionTime) {
        		//transition mode.
        		float progress = (float)(time-timeSinceTransition)/(float)transitionTime;
        		displaypaint.setAlpha((int)((1-progress)*255));
            	if (oldProcessedWaveform != null) c.drawBitmap(oldProcessedWaveform, offset, 0, displaypaint);
            	displaypaint.setAlpha((int)(progress*255));
            	c.drawBitmap(processedWaveform, offset, 0, displaypaint);
        	} else {
//        		int ms = Math.abs((int)(time % 5000) - 2500);
//        		displaypaint.setAlpha(200+ms/50);
        		displaypaint.setAlpha(255);
            	c.drawBitmap(processedWaveform, offset, 0, displaypaint);

        	}
        	
        	
		
        }
        c.restore();

	}
	
	
}
