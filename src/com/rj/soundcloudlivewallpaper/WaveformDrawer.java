package com.rj.soundcloudlivewallpaper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.TypedValue;

/**
 * The new and revised waveform drawer - now using more useful data!
 * by taking the float[] data, we can make much cooler lines, etc
 * and it's actually faster and WAY more memory efficient.
 * @author rj
 *
 */
public class WaveformDrawer {
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
	float scale = 0.9f;
	float linegap;
	ColorsMap map;
	long transitionStartTime;
	long transitionDuration = 4*1000L;
	int  colorDuration = 7*1000;
	
	String loadingtext = "Loading...";
	
	public WaveformDrawer(Context context) {
		Resources res = context.getResources();
		linepaint = new Paint();
		linepaint.setStrokeWidth(strokeWidth);
		linepaint.setAntiAlias(true);
    	linepaint.setColor(res.getColor(R.color.bar_color));
    	linepaint.setTextAlign(Align.CENTER);
    	linepaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
    			              res.getInteger(R.integer.loading_text_size), 
    			              res.getDisplayMetrics()));
    	loadingtext = res.getString(R.string.loading);
    	transitionDuration = res.getInteger(R.integer.bar_transition_time);
    	colorDuration = res.getInteger(R.integer.color_transition_time);
    	horizontalscale = res.getInteger(R.integer.horizontal_scale);
    	strokeWidth = res.getInteger(R.integer.stroke_width);
    	scale = res.getInteger(R.integer.bar_scale_percent)/100f;
    	map = new ColorsMap(ColorsMap.fromResources(ColorsMap.DEFAULT_RESOURCES, context), colorDuration);
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
			float top = halfheight - halfheight * value * scale;
			float bottom = halfheight + halfheight * value * scale;
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
        	
        	//precalculate a ton of points
        	//whenever I'm in a draw loop, I finalize variables.
        	//I feel this is like putting lightning-bolt stickers on a laptop and hoping it goes faster.
            final int showingpoints = points.length / horizontalscale;
            final int remainingpoints = (points.length - showingpoints);
            final int speed = 2000000;
            totaloffset = ((float)(System.currentTimeMillis()%speed)/(float)speed * remainingpoints);
            final float position = (totaloffset);
            int startingindex = (int) position;
            startingindex = startingindex - startingindex % 4; //make it end on 4.
            final float residual = position - startingindex;

            float left = -points[startingindex];
            left = left - linegap*residual;
            left = left - mOffset*linegap*showingpoints/8;
            c.translate(left, 0);
        	
            //so it's actually worth noting exactly how this works:
            //we have a bit bucket of data of our lines, and it's rather immutable
            //so we can't change it every frame. what do we do instead? the opposite.
            //move the canvas around it.
            //hence the translate.
            //but we also need to cut down on how much we tell the canvas to draw
            //so we calculate the 'showing points', which we feed to drawLines
            
            long timediff = System.currentTimeMillis() - transitionStartTime;
            if (timediff <= transitionDuration) {
            	final float ratio = timediff/(float)transitionDuration;
            	
            	//long start = System.currentTimeMillis();
            	
            	linepaint.setStrokeWidth((1-ratio)*strokeWidth);
            	//linepaint.setAlpha(Math.round((1-ratio)*255));
            	if (oldpoints != null) c.drawLines(oldpoints, startingindex, showingpoints/4, linepaint);
            	
            	linepaint.setStrokeWidth(ratio*strokeWidth);
            	//linepaint.setAlpha(Math.round(ratio*255));
            	c.drawLines(points, startingindex, showingpoints/4, linepaint);
            	
            	//long end = System.currentTimeMillis();
            	//Log.d(TAG, "total time for all those lines (transitin): "+(end-start));

            } else {
            	linepaint.setStrokeWidth(strokeWidth);
            	//linepaint.setAlpha(150);
            	
//            	long start = System.currentTimeMillis();
            	c.drawLines(points, startingindex, showingpoints/4, linepaint);
//            	long end = System.currentTimeMillis();
//            	Log.d(TAG, "total time for all those lines: "+(end-start));
            }
            
        	
		
        } else {
        	c.drawText(loadingtext, width/2, height/2, linepaint);
        }
        c.restore();

	}
	
	
}
