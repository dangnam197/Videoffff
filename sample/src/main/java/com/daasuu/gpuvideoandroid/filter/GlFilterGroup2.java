package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;
import android.util.Pair;

import com.daasuu.gpuv.egl.GlFramebufferObject;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;

import java.util.ArrayList;
import java.util.Collection;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;


public class GlFilterGroup2 extends GlFilter {

    private final Collection<GlFilter> filters = new ArrayList<>();

    private final ArrayList<Pair<GlFilter, GlFramebufferObject>> list = new ArrayList<Pair<GlFilter, GlFramebufferObject>>();

//    private Bitmap bitmap;
//    private Bitmap bitmap2;
    private GlFilterGroup glFilterGroup;

    private GlBitmapTranslate2 gl1;
    private GlBitmapBlur glBlur1;

    private GlBitmapTranslate2 gl2;
    private GlBitmapBlur glBlur2;
    private int start;
    private int end;
    public GlFilterGroup2(Bitmap bitmap, Bitmap bitmap2,int start,int end) {
        this.start = start;
        this.end = end;
//        gl1 = new GlBitmapO1(bitmap,start,end);
        glBlur1 = new GlBitmapBlur(bitmap,start,end);

//        gl2 = new GlBitmapO1(bitmap2,start,end);
        glBlur2 = new GlBitmapBlur(bitmap2,start,end);

        filters.add(glBlur1);
        filters.add(gl1);

//        filters.add(glBlur2);
//        filters.add(gl2);
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
    }

    private int prevTexName;

    @Override
    public void draw(final int texName, final GlFramebufferObject fbo) {
        if(fbo != null) {
            Log.d("testTime", "drawCanvas2: " + fbo.getCurrentTime());
        }
        super.draw(texName,fbo);
        if(fbo!=null&&start < fbo.getCurrentTime() && fbo.getCurrentTime() < end) {
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
        if(start < time && time < end) {
//            Log.d("testTime", "drawCanvas2: "+time);
//            gl1.setTranslate(-width * ((float)(time - start))/(end - start),0);
            glBlur1.setTranslate(-width * ((float)(time - start))/(end - start),0);

//            gl2.setTranslate(width-width * ((float)(time - start))/(end - start),0);
            glBlur2.setTranslate(width-width * ((float)(time - start))/(end - start),0);

        }
    }

}
