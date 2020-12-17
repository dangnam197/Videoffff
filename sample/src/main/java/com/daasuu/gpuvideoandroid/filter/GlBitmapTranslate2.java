package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.daasuu.gpuv.egl.filter.GlOverlayFilter;

import io.alterac.blurkit.BlurKit;

public class GlBitmapTranslate2 extends GlOverlayFilter {

    private Bitmap originBitmap1;
    private Bitmap originBitmap2;

    private Bitmap bitmapDraw1;
    private Bitmap bitmapDraw2;
    private final Matrix matrix1 = new Matrix();
    private final Matrix matrix2 = new Matrix();

    private int start = 0;
    private int end = 0;

    private static final String TAG = "GlBitmapOverlaySample";
    public void setTranslateMatrix1(float x,float y){
        matrix1.setTranslate(x,y);
    }
    public void setTranslateMatrix2(float x,float y){
        matrix2.setTranslate(x,y);
    }
    public GlBitmapTranslate2(Bitmap bitmap1, Bitmap bitmap2, int start, int end) {
        this.originBitmap1 = bitmap1;
        this.originBitmap2 = bitmap2;
        this.bitmapDraw1 = bitmap1;
        this.bitmapDraw2 = bitmap2;
        this.start = start;
        this.end = end;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setSizeBitmap(width,height);
    }

    private void setSizeBitmap(int width, int height) {
        long time = System.currentTimeMillis();
        bitmapDraw1 = getBitmapTranslate(originBitmap1,width,height);
        bitmapDraw2 = getBitmapTranslate(originBitmap2,width,height);

        Log.d("timeTesst", "setSizeBitmap: "+(System.currentTimeMillis() - time));
    }
    private Bitmap getBitmapTranslate(Bitmap bitmap,int width, int height){
        Bitmap bitmapBlur = BlurKit.getInstance().blur(bitmap, 10);
        Bitmap bitmapTranslate = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvasTranslate = new Canvas(bitmapTranslate);
        drawBitmapBackground(canvasTranslate,bitmapBlur,width,height);
        drawBitmap(canvasTranslate,bitmap,width,height);
        return bitmapTranslate;
    }

    private void drawBitmap(Canvas canvas, Bitmap srcBitmap, int width, int height) {
        float screenRatio = (float) width / height;
        float srcRatio = (float) srcBitmap.getWidth() / srcBitmap.getHeight();

        canvas.save();
        Rect srcRect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        Rect dstRect = null;
        if (srcRatio > screenRatio) {
            int dstH = (int) (width / srcRatio);
            int spaceH = (height - dstH) / 2;
            dstRect = new Rect(0, spaceH, width, dstH + spaceH);
        } else {
            int dstW = (int) (height * srcRatio);
            int spaceW = (width - dstW) / 2;
            dstRect = new Rect(spaceW, 0, dstW + spaceW, height);
        }
        canvas.drawBitmap(srcBitmap, srcRect, dstRect, null);
        canvas.restore();
    }

    private void drawBitmapBackground(Canvas canvas, Bitmap srcBitmap, int width, int height) {
        float screenRatio = (float) width / height;
        float srcRatio = (float) srcBitmap.getWidth() / srcBitmap.getHeight();
        canvas.save();
        Rect srcRect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        Rect dstRect = null;
        dstRect = new Rect(0, 0, width, height);
        if (screenRatio > srcRatio) {
            int dstH = (int) (srcRect.width() / screenRatio);
            int spaceH = (srcRect.height() - dstH) / 2;
            srcRect = new Rect(0, spaceH, srcRect.width(), dstH + spaceH);
        } else {
            int dstW = (int) (srcRect.height() * screenRatio);
            int spaceW = (srcRect.width() - dstW) / 2;
            srcRect = new Rect(spaceW, 0, dstW + spaceW, srcRect.height());
        }
        canvas.drawBitmap(srcBitmap, srcRect, dstRect, null);
        canvas.restore();
    }
    @Override
    protected void drawCanvas(Canvas canvas, long time, int width, int height) {
        canvas.save();
        if(time > start&&time < end) {
            anim(time, width, height);
            canvas.drawBitmap(bitmapDraw2, matrix2, null);
            canvas.drawBitmap(bitmapDraw1, matrix1, null);
        }
        canvas.restore();
    }
    private void anim(long time, int width, int height) {
        if (start < time && time < end) {
            setTranslateMatrix1(-width * ((float) (time - start)) / (end - start), 0);
            setTranslateMatrix2(0,-height * ((float) (time - start)) / (end - start));
        }
    }
}
