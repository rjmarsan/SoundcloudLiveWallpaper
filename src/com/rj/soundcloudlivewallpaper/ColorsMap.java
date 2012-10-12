package com.rj.soundcloudlivewallpaper;

import java.util.Random;

import android.graphics.Color;
import android.util.Log;
/**
 * Simple little color interpolater. it's nice.
 * @author rj
 *
 */
public class ColorsMap {
	public final static int[] DEFAULT_MAP = {
		0xffB81D1D, 
		0xffE6D1BA,
		0xff41C4A3, 
		0xff476D7A, 
		0xff444A78, 
		0xffFCAD4C, 
		0xffF25555,
//		0xff47263A, 
		0xffF0E07D, 
		0xffB5CC9D, 
	};
	
	int[] colors;
	long transitionms;
	int currentColorIndex;
	int nextColorIndex;
	long starttime;
	Random random = new Random();
	
	public ColorsMap(int[] colors, long transitionms) {
		this.colors = colors;
		this.transitionms = transitionms;
		currentColorIndex = 0;
		pickNextColor();
		starttime = System.currentTimeMillis();
	}
	
	public void pickNextColor() {
		int nextColor = random.nextInt(colors.length);
		while (nextColor == currentColorIndex) {
			nextColor = random.nextInt(colors.length);
		}
		currentColorIndex = nextColorIndex;
		nextColorIndex = nextColor;
	}
	
	public int getColor() {
		long now = System.currentTimeMillis();
		long diff = now-starttime;
		if (diff >= transitionms) {
			pickNextColor();
			starttime = System.currentTimeMillis();
			return colors[currentColorIndex];
		}
		float ratio = (float)diff/(float)transitionms;
		//Log.d("ColorsMap", "Ratio: "+ratio);
		int color1 = colors[currentColorIndex];
		int color2 = colors[nextColorIndex];
		return interpolateColor(color1,color2,ratio);
	}
	
	private int interpolateColor(int color1, int color2, float ratio) {
		int r1 = Color.red(color1);
		int g1 = Color.green(color1);
		int b1 = Color.blue(color1);
		int r2 = Color.red(color2);
		int g2 = Color.green(color2);
		int b2 = Color.blue(color2);
		return Color.rgb(interpolate(r1,r2,ratio),
				interpolate(g1,g2,ratio),
				interpolate(b1,b2,ratio));
	}
	private int interpolate(int val1, int val2, float ratio) {
		return (int)(val1*(1-ratio) + val2*ratio);
	}
}
