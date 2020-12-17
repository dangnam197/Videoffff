package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlOverlayFilter;
import com.daasuu.gpuv.egl.filter.GlOverlayGaussianBlurFilter;

public class GlBitmapBlur extends GlOverlayGaussianBlurFilter {

    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private long beforeTime = 0;
    private int start = 0;
    private int end = 0;
    private Rect srcRect;
    private Rect dstRect;
    private float srcRatio = 1f;
    private static final String TAG = "GlBitmapOverlaySample";

    public GlBitmapBlur(Bitmap bitmap, int start, int end) {
        this.bitmap = bitmap;
        srcRect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        srcRatio = (float)bitmap.getWidth()/bitmap.getHeight();
        this.start = start;
        this.end = end;
    }
    @Override
    protected void drawCanvas(Canvas canvas, long time, int width, int height) {
        float screenRatio = (float)width/height;
        canvas.save();
        canvas.setMatrix(matrix);
        if(dstRect == null) {
            dstRect = new Rect(0,0,width,height);
            if (screenRatio > srcRatio) {
                int dstH = (int) (srcRect.width()/screenRatio);
                int spaceH = (srcRect.height() - dstH)/2;
                srcRect = new Rect(0,spaceH,srcRect.width(),dstH+spaceH);
            }else {
                int dstW = (int) (srcRect.height()*screenRatio);
                int spaceW = (srcRect.width() - dstW)/2;
                srcRect = new Rect(spaceW,0,dstW+spaceW,srcRect.height());
            }
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            canvas.drawBitmap(bitmap, srcRect, dstRect, null);
        }
        canvas.restore();
        beforeTime = time;
    }

    public void setTranslate(float x, float y) {
        matrix.setTranslate(x,y);
    }
}
