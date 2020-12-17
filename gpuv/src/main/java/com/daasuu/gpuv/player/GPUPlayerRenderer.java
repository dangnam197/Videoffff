package com.daasuu.gpuv.player;

import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.daasuu.gpuv.egl.EglUtil;
import com.daasuu.gpuv.egl.GlFrameBufferObjectRenderer;
import com.daasuu.gpuv.egl.GlFramebufferObject;
import com.daasuu.gpuv.egl.GlPreviewFilter;
import com.daasuu.gpuv.egl.GlSurfaceTexture;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.filter.GlLookUpTableFilter;
import com.daasuu.gpuv.egl.filter.GlPreviewFilter2;
import com.google.android.exoplayer2.SimpleExoPlayer;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glViewport;

public class GPUPlayerRenderer extends GlFrameBufferObjectRenderer implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = GPUPlayerRenderer.class.getSimpleName();

    private GlSurfaceTexture previewTexture;

    private boolean updateSurface = false;

    private int texName;

    private float[] MVPMatrix = new float[16];
    private float[] MVPMatrix2 = new float[16];


    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];
    private float[] STMatrix = new float[16];


    private GlFramebufferObject filterFramebufferObject;
    private GlPreviewFilter previewFilter;
    private GlPreviewFilter2 previewFilter2;


    private GlFilter glFilter;
    private boolean isNewFilter;
    private final GPUPlayerView glPreview;

    private float aspectRatio = 1f;

    private SimpleExoPlayer simpleExoPlayer;
    private SimpleExoPlayer simpleExoPlayer2;
    private long currentTime = 0L;
    private long beforeTime = 0L;

    GPUPlayerRenderer(GPUPlayerView glPreview) {
        super();
        Matrix.setIdentityM(STMatrix, 0);
        this.glPreview = glPreview;
    }

    void setGlFilter(final GlFilter filter) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (glFilter != null) {
                    glFilter.release();
                    if (glFilter instanceof GlLookUpTableFilter) {
                        ((GlLookUpTableFilter) glFilter).releaseLutBitmap();
                    }
                    glFilter = null;
                }
                glFilter = filter;
                isNewFilter = true;
                glPreview.requestRender();
            }
        });
    }

    @Override
    public void onSurfaceCreated(final EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        final int[] args = new int[1];

        GLES20.glGenTextures(args.length, args, 0);
        texName = args[0];


        previewTexture = new GlSurfaceTexture(texName);

        previewTexture.setOnFrameAvailableListener(this);


        GLES20.glBindTexture(previewTexture.getTextureTarget(), texName);
        // GL_TEXTURE_EXTERNAL_OES
        EglUtil.setupSampler(previewTexture.getTextureTarget(), GL_LINEAR, GL_NEAREST);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);

        filterFramebufferObject = new GlFramebufferObject();
        // GL_TEXTURE_EXTERNAL_OES
        previewFilter = new GlPreviewFilter(previewTexture.getTextureTarget());
        previewFilter.setup();

        previewFilter2 = new GlPreviewFilter2(previewTexture.getTextureTarget());
        previewFilter2.setup();

        Surface surface = new Surface(previewTexture.getSurfaceTexture());
        this.simpleExoPlayer.setVideoSurface(surface);


//        this.simpleExoPlayer2.setVideoSurface(surface);

        Matrix.setLookAtM(VMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );

        synchronized (this) {
            updateSurface = false;
        }

        if (glFilter != null) {
            isNewFilter = true;
        }

        GLES20.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);

    }

    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);
        filterFramebufferObject.setup(width, height);
        previewFilter.setFrameSize(width, width);
        previewFilter2.setFrameSize(width, width);

        if (glFilter != null) {
            glFilter.setFrameSize(width, width);
        }

        aspectRatio = (float) width / height;
        Matrix.frustumM(ProjMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);
    }

    @Override
    public void onDrawFrame(final GlFramebufferObject fbo) {

        currentTime = simpleExoPlayer.getCurrentPosition();

        Log.d(TAG, "onDrawFrame: " + fbo.toString() + "  -  " + currentTime);
        fbo.setCurrentTime(currentTime);
        synchronized (this) {
            if (updateSurface) {
                previewTexture.updateTexImage();
                previewTexture.getTransformMatrix(STMatrix);
                updateSurface = false;
            }
        }

        if (isNewFilter) {
            if (glFilter != null) {
                glFilter.setup();
                glFilter.setFrameSize(fbo.getWidth(), fbo.getHeight());
            }
            isNewFilter = false;
        }

        if (glFilter != null) {
            filterFramebufferObject.enable();
            glViewport(0, 0, filterFramebufferObject.getWidth(), filterFramebufferObject.getHeight());
        }

        GLES20.glClear(GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);
        System.arraycopy(MVPMatrix, 0, MVPMatrix2, 0, MVPMatrix.length);

        Matrix.scaleM(MVPMatrix2,0,scaleVideoBlurX,scaleVideoBlurY,1f);
        Matrix.rotateM(MVPMatrix2, 0, rotateVideo, 0.f, 0.f, 1.f);
        Matrix.translateM(MVPMatrix2,0,translateX,translateY,0);

//        Matrix.translateM(MVPMatrix,0,-1f,1f,0);
        calculateScale();
        Matrix.scaleM(MVPMatrix,0,scaleVideoX,scaleVideoY,1f);
        Matrix.rotateM(MVPMatrix, 0, rotateVideo, 0.f, 0.f, 1.f);
        Matrix.translateM(MVPMatrix,0,translateX,translateY,0);


//        Matrix.rotateM(MVPMatrix,0,45,0,0,0.1f);

//        previewFilter.setCrop(cropRectF);
        previewFilter2.draw(texName, MVPMatrix2, STMatrix, aspectRatio);
        previewFilter.draw(texName, MVPMatrix, STMatrix, aspectRatio);


        if (glFilter != null) {
            fbo.enable();
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            glFilter.draw(filterFramebufferObject.getTexName(), fbo);
        }
    }
    private RectF cropRectF = new RectF(0.5f,0f,1f,-1f);
    private float ratioVideoReal =2f;
    private float ratioScreen = 1f;
    private float ratioVideo = 2f;
    private float scaleVideoX = 1f;
    private float scaleVideoY = 1f;
    private float scaleVideoBlurX = 1f;
    private float scaleVideoBlurY = 1f;

    private float translateX = 0f;
    private float translateY = 0f;
    private float rotateVideo = 0f;
    public void calculateScale(){
        if(rotateVideo % 180 != 0){
            ratioVideo = 1/ratioVideoReal;
        }
        if(ratioScreen > ratioVideo){
            scaleVideoY = 1f;
            scaleVideoX = ratioScreen*ratioVideo;

            scaleVideoBlurX = 1f;
            scaleVideoBlurY = ratioScreen/ratioVideo;
        }else {
            scaleVideoX = 1f;
            scaleVideoY = ratioScreen/ratioVideo;

            scaleVideoBlurY = 1f;
            scaleVideoBlurX = ratioVideo*ratioScreen;
        }

        float ratioCrop = cropRectF.width()/cropRectF.height();
        float scaleCrop = 1f;
        if(rotateVideo % 180 != 0){
            ratioCrop = 1/ratioCrop;
        }
        if(ratioScreen > ratioCrop){
            scaleCrop = 2f/cropRectF.height();
            translateX = -(cropRectF.left + cropRectF.right)/2f;
            translateY = 0f;
            previewFilter.resetCrop();
            previewFilter.setCropLeft((cropRectF.left+1)/2f);
            previewFilter.setCropRight((cropRectF.right+1)/2f);

        }else {
            translateY = -(cropRectF.top + cropRectF.bottom)/2f;
            translateX = 0f;
            scaleCrop = 2f/cropRectF.width();
            previewFilter.resetCrop();
            previewFilter.setCropTop((cropRectF.top+1)/2f);
            previewFilter.setCropBottom((cropRectF.bottom+1)/2f);
        }
        scaleCrop = Math.abs(scaleCrop);
        scaleVideoY = scaleVideoY*scaleCrop;
        scaleVideoX = scaleVideoX*scaleCrop;
        scaleVideoBlurX = scaleVideoBlurX*scaleCrop;
        scaleVideoBlurY = scaleVideoBlurY*scaleCrop;
    }
    @Override
    public synchronized void onFrameAvailable(final SurfaceTexture previewTexture) {
        updateSurface = true;
        glPreview.requestRender();
    }

    void setSimpleExoPlayer(SimpleExoPlayer simpleExoPlayer, SimpleExoPlayer simpleExoPlayer2) {
        this.simpleExoPlayer = simpleExoPlayer;
        this.simpleExoPlayer2 = simpleExoPlayer2;

    }

    void release() {
        if (glFilter != null) {
            glFilter.release();
        }
        if (previewTexture != null) {
            previewTexture.release();
        }
    }

}
