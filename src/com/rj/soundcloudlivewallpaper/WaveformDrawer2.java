package com.rj.soundcloudlivewallpaper;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class WaveformDrawer2 {
	public final String TAG = "WaveformDrawer2";
	float[] waveform;
	float[] points;
	float[] oldpoints;
	int width = -1;
	int height = -1;
	Paint linepaint;
	int horizontalscale = 12;
	float strokeWidth = 8;
	float totaloffset = 0;
	float linegap;
	ColorsMap map = new ColorsMap(ColorsMap.DEFAULT_MAP, 7000);
	long transitionStartTime;
	long transitionDuration = 4*1000L;
	
	public WaveformDrawer2(Context context) {
		linepaint = new Paint();
		linepaint.setStrokeWidth(strokeWidth);
		linepaint.setAntiAlias(true);
	}
	
	public void cleanup() {
	}
	
	public void setWaveform(String path) {
		setWaveform(BitmapFactory.decodeFile(path));
	}
	public void setWaveform(Bitmap bitmap) {
		setAndPreprocessBitmap(bitmap);
	}
	
	
	/**
	 * Doing effects, etc, that we don't need to do every draw()
	 * @param bitmap
	 */
	private void setAndPreprocessBitmap(Bitmap bitmap) {
		waveform = WaveformProcessor.getWaveformFromBitmap(bitmap);
		if (width > 0 && height > 0) generateLines(waveform, width, height);
	}
	
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		if (waveform != null) generateLines(waveform, width, height);
	}
	
	public void generateLines(float[] waveform, float width, float height) {
		width = width * horizontalscale;
		float[] points = new float[waveform.length * 4]; //2 points for every bar. 2 coords per point.
		this.linegap = width/waveform.length;
		float halfheight = height/2f;
		for (int i=0; i<points.length; i+=4) {
			float value = waveform[i/4];
			float left = linegap * i;
			float top = halfheight - halfheight * value;
			float bottom = halfheight + halfheight * value;
			points[i+0] = left;
			points[i+1] = top;
			points[i+2] = left;
			points[i+3] = bottom;			
		}
		transitionStartTime = System.currentTimeMillis();
		this.oldpoints = this.points;
		this.points = points;
	}
	
	
	public void draw(Canvas c, float mOffset) {
		
		
        c.save();
        c.drawColor(map.getColor());
        if (points != null) {
            int showingpoints = points.length / horizontalscale;
            int remainingpoints = (points.length - showingpoints);
            int speed = 2000000;
            totaloffset = ((float)(System.currentTimeMillis()%speed)/(float)speed * remainingpoints);
            float position = (totaloffset);
            int startingindex = (int) position;
            startingindex = startingindex - startingindex % 4; //make it end on 4.
            float residual = position - startingindex;

            float left = -points[startingindex];
            left = left - linegap*residual;
            left = left - mOffset*linegap*showingpoints/8;
            c.translate(left, 0);
        	//c.scale(10, 1);

            long timediff = System.currentTimeMillis() - transitionStartTime;
            if (timediff <= transitionDuration) {
            	float ratio = timediff/(float)transitionDuration;
            	
            	long start = System.currentTimeMillis();
            	
            	linepaint.setStrokeWidth((1-ratio)*strokeWidth);
            	if (oldpoints != null) c.drawLines(oldpoints, startingindex, showingpoints/4, linepaint);
            	
            	linepaint.setStrokeWidth(ratio*strokeWidth);
            	c.drawLines(points, startingindex, showingpoints/4, linepaint);
            	
            	long end = System.currentTimeMillis();
            	Log.d(TAG, "total time for all those lines (transitin): "+(end-start));

            } else {
            	linepaint.setStrokeWidth(strokeWidth);
            	
            	long start = System.currentTimeMillis();
            	//c.drawLines(points, linepaint);
            	c.drawLines(points, startingindex, showingpoints/4, linepaint);
            	long end = System.currentTimeMillis();
            	Log.d(TAG, "total time for all those lines: "+(end-start));
            }
            
        	
		
        }
        c.restore();

	}
	
	
}
