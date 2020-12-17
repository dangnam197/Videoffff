package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;
import android.util.Pair;

import com.daasuu.gpuv.egl.GlFramebufferObject;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuvideoandroid.R;

import java.util.ArrayList;
import java.util.Collection;

import io.alterac.blurkit.BlurKit;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;


public class GlFilterTranslate extends GlFilter {

    private final Collection<GlFilter> filters = new ArrayList<>();

    private final ArrayList<Pair<GlFilter, GlFramebufferObject>> list = new ArrayList<Pair<GlFilter, GlFramebufferObject>>();

    private Bitmap bitmap;
    private Bitmap bitmap2;

    private GlBitmapTranslate gl1;

    private GlBitmapTranslate gl2;
    private int start;
    private int end;

    public GlFilterTranslate(Bitmap bitmap, Bitmap bitmap2, int start, int end) {
        this.bitmap = bitmap;
        this.bitmap2 = bitmap2;
        this.start = start;
        this.end = end;
        gl2 = new GlBitmapTranslate(bitmap2);
        gl1 = new GlBitmapTranslate(bitmap);

        filters.add(gl2);
        filters.add(gl1);
    }

    @Override
    public void setup() {
        super.setup();

        if (filters != null) {
            final int max = filters.size();
            int count = 0;

            for (final GlFilter shader : filters) {
                shader.setup();
                final GlFramebufferObject fbo;
                if ((count + 1) < max) {
                    fbo = new GlFramebufferObject();
                } else {
                    fbo = null;
                }
                list.add(Pair.create(shader, fbo));
                count++;
            }
        }
    }

    @Override
    public void release() {
        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.release();
            }
            if (pair.second != null) {
                pair.second.release();
            }
        }
        list.clear();
        super.release();
    }

    @Override
    public void setFrameSize(final int width, final int height) {
        super.setFrameSize(width, height);
        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.setFrameSize(width, height);
            }
            if (pair.second != null) {
                pair.second.setup(width, height);
            }
        }
        setSizeBitmap(width, height);
    }

    private void setSizeBitmap(int width, int height) {
        long time = System.currentTimeMillis();
        gl1.setBitmap(getBitmapTranslate(bitmap,width,height));
        gl2.setBitmap(getBitmapTranslate(bitmap2,width,height));

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

    private int prevTexName;

    @Override
    public void draw(final int texName, final GlFramebufferObject fbo) {
        if (fbo != null) {
            Log.d("testTime", "drawCanvas2: " + fbo.getCurrentTime());
        }
        super.draw(texName, fbo);
        if (fbo != null && start < fbo.getCurrentTime() && fbo.getCurrentTime() < end) {
            anim(fbo.getCurrentTime(), fbo.getWidth(), fbo.getHeight());
            prevTexName = texName;
            for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
                if (pair.second != null) {
                    if (pair.first != null) {
                        pair.second.enable();
                        GLES20.glClear(GL_COLOR_BUFFER_BIT);

                        pair.first.draw(prevTexName, pair.second);
                    }
                    prevTexName = pair.second.getTexName();

                } else {
                    if (fbo != null) {
                        fbo.enable();
                    } else {
                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                    }

                    if (pair.first != null) {
                        pair.first.draw(prevTexName, fbo);
                    }
                }
            }
        }
//        else {
//            super.draw(texName,fbo);
//        }
    }

    private void anim(long time, int width, int height) {
        if (start < time && time < end) {
            gl1.setTranslate(-width * ((float) (time - start)) / (end - start), 0);
            gl2.setTranslate(width - width * ((float) (time - start)) / (end - start), 0);
        }
    }


}
