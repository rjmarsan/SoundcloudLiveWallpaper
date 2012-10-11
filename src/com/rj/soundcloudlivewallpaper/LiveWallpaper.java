package com.rj.soundcloudlivewallpaper;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * This is a clear fork of the CubeLiveWallpaper example android includes.
 * @author rj
 *
 */
public class LiveWallpaper extends WallpaperService {

    private final Handler mHandler = new Handler();
    
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        return new WaveformEngine();
    }

    
    
    public BackgroundManager getBackgroundManager() {
    	return new BackgroundManager(getApplicationContext());
    }
    
    
    class WaveformEngine extends Engine {

        private float mOffset;
        private BackgroundManager manager;

        private final Runnable mDrawCube = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        //allows the manager to trigger draw requests.
        private final Runnable mRequestDrawCube = new Runnable() {
        	public void run() {
        		mHandler.post(mDrawCube);
        	}
        };

        WaveformEngine() {
        	manager = getBackgroundManager();
        	manager.setRequestDrawRunnable(mRequestDrawCube);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            // By default we don't get touch events, so enable them.
            //setTouchEventsEnabled(true); //do we need this?
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            stop();
            manager.cleanup();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                start();
            } else {
                stop();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            manager.resize(width, height);
            start();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            stop();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) {
            mOffset = xOffset;
            drawFrame();
        }

        private void stop() {
        	mHandler.removeCallbacks(mDrawCube);
        	manager.stop();
        }
        private void start() {
        	manager.start();
        	drawFrame();
        }
        
        @Override
        public Bundle onCommand(String action, int x, int y, int z,
        		Bundle extras, boolean resultRequested) {
        	return super.onCommand(action, x, y, z, extras, resultRequested);
        	//this is where I handle clickthrough
        }

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    // draw something
                	manager.draw(c, mOffset);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

        }


    }

}
