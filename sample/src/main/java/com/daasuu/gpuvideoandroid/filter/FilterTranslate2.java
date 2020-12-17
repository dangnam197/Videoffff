package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.daasuu.gpuv.egl.GlFramebufferObject;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

public class FilterTranslate2 extends GlFilter{
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private GlFilterGroup glFilterGroup;

    private GlBitmapTranslate2 gl1;
    private GlBitmapBlur glBlur1;

    private GlBitmapTranslate2 gl2;
    private GlBitmapBlur glBlur2;
    private ArrayList<GlFilter> list = new ArrayList<>();
    public FilterTranslate2(Bitmap bitmap, Bitmap bitmap2,int start,int end) {
        this.bitmap = bitmap;
        this.bitmap2 = bitmap2;
//        gl1 = new GlBitmapO1(bitmap,start,end);
        glBlur1 = new GlBitmapBlur(bitmap,start,end);

//        gl2 = new GlBitmapO1(bitmap2,start,end);
        glBlur2 = new GlBitmapBlur(bitmap2,start,end);
    }
    public GlFilterGroup getFilter(){
        return glFilterGroup;
    }

    @Override
    public void setup() {
        super.setup();
        gl1.setup();
        glBlur1.setup();
        gl2.setup();
        glBlur2.setup();
//        if (filters != null) {
//            final int max = filters.size();
//            int count = 0;
//
//            for (final GlFilter shader : filters) {
//                shader.setup();
//                final GlFramebufferObject fbo;
//                if ((count + 1) < max) {
//                    fbo = new GlFramebufferObject();
//                } else {
//                    fbo = null;
//                }
//                list.add(Pair.create(shader, fbo));
//                count++;
//            }
//        }
    }

    @Override
    public void release() {
        gl1.release();
        glBlur1.release();
        gl2.release();
        glBlur2.release();
        super.release();
    }


    @Override
    public void setFrameSize(final int width, final int height) {
        super.setFrameSize(width, height);
        gl1.setFrameSize(width,height);
        glBlur1.setFrameSize(width,height);
        gl2.setFrameSize(width,height);
        glBlur2.setFrameSize(width,height);
    }

    private int prevTexName;

//    @Override
//    public void onDraw(long time, int width, int height) {
//        super.onDraw(time, width, height);
////        glBlur1.onDraw(time,width,height);
////        gl1.onDraw(time,width,height);
////        glBlur2.onDraw(time,width,height);
////        gl2.onDraw(time,width,height);
//    }

    @Override
    public void draw(final int texName, final GlFramebufferObject fbo) {
        prevTexName = texName;
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        glBlur1.draw(prevTexName,fbo);
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        gl1.draw(prevTexName,fbo);
//        glBlur2.draw(prevTexName,fbo);
//        gl2.draw(prevTexName,fbo);
//        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
//            if (pair.second != null) {
//                if (pair.first != null) {
//                    pair.second.enable();
//                    GLES20.glClear(GL_COLOR_BUFFER_BIT);
//
//                    pair.first.draw(prevTexName, pair.second);
//                }
//                prevTexName = pair.second.getTexName();
//
//            } else {
//                if (fbo != null) {
//                    fbo.enable();
//                } else {
//                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
//                }
//
//                if (pair.first != null) {
//                    pair.first.draw(prevTexName, fbo);
//                }
//            }
//        }
    }
}
