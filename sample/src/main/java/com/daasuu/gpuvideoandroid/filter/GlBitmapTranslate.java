package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlOverlayFilter;

public class GlBitmapTranslate extends GlOverlayFilter {

    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private static final String TAG = "GlBitmapOverlaySample";
    private boolean check = true;
    public GlBitmapTranslate(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.check = true;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.check = true;
    }
    public void setTranslate(float x,float y){
        matrix.setTranslate(x,y);
        this.check = true;
    }
    @Override
    protected void drawCanvas(Canvas canvas) {
        canvas.drawBitmap(bitmap,matrix,null);
        this.check = false;
    }
    @Override
    protected void drawCanvas(Canvas canvas,long time,int width,int height) {
        long timeZ = System.currentTimeMillis();
        canvas.drawBitmap(bitmap,matrix,null);
        this.check = false;
        Log.d(TAG, time+" drawCanvasTime: "+(System.currentTimeMillis() - timeZ));
    }
    @Override
    protected boolean checkDraw() {
        return check;
    }
}
