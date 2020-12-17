package com.daasuu.gpuvideoandroid.filter;

import android.graphics.Bitmap;

import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;

import java.util.ArrayList;

public class FilterTranslate {
    private Bitmap bitmap;
    private Bitmap bitmap2;
    private GlFilterGroup glFilterGroup;
    private ArrayList<GlFilter> list = new ArrayList<>();
    public FilterTranslate(Bitmap bitmap, Bitmap bitmap2) {
        this.bitmap = bitmap;
        this.bitmap2 = bitmap2;
        list.add(new GlBitmapBlur(bitmap,5000,8000));
//        list.add(new GlBitmapO1(bitmap2,5000,8000));
        glFilterGroup = new GlFilterGroup(list);
    }
    public GlFilterGroup getFilter(){
        return glFilterGroup;
    }
}
