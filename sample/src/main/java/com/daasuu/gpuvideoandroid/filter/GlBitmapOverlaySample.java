package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlOverlayFilter;

public class GlBitmapOverlaySample extends GlOverlayFilter {

    private Bitmap bitmap;
    private Bitmap bitmap2;
    private Matrix matrix = new Matrix();
    private long beforeTime = 0;
    private static final String TAG = "GlBitmapOverlaySample";
    public GlBitmapOverlaySample(Bitmap bitmap,Bitmap bitmap2) {
        this.bitmap = bitmap;
        this.bitmap2 = bitmap2;
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        Log.d(TAG, "drawCanvas: ");
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }
    }
    @Override
    protected void drawCanvas(Canvas canvas,long time,int width,int height) {
        Log.d(TAG, "drawCanvas2: "+time);
//        if(time > 5000&&time < 10000) {
        canvas.save();
//            matrix.setTranslate(-time/10f,0f);
//            canvas.setMatrix(matrix);
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, -time/10, 0, null);
            }
            if (bitmap2 != null && !bitmap2.isRecycled()) {
                canvas.drawBitmap(bitmap2, width - time/10, 0, null);
            }
//        }
        canvas.restore();
        beforeTime = time;
    }

}
