package com.daasuu.gpuv.egl.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.Size;


public class GlOverlayGaussianBlurFilter extends GlFilter {

    private int[] textures = new int[1];
    private Bitmap bitmap = null;
    private Bitmap bitmapBlur = null;
    protected Size inputResolution = new Size(1280, 720);

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;" +
                    "attribute vec4 aTextureCoord;" +

                    "const lowp int GAUSSIAN_SAMPLES = 9;" +
                    "varying highp vec2 vTextureCoord;" +
                    "uniform highp float texelWidthOffset;" +
                    "uniform highp float texelHeightOffset;" +
                    "uniform highp float blurSize2;\n" +


                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +

                    "void main() {" +
                    "gl_Position = aPosition;" +
                    "vTextureCoord = aTextureCoord.xy;" +

                    // Calculate the positions for the blur
                    "int multiplier = 0;" +
                    "highp vec2 blurStep;" +
                    "highp vec2 singleStepOffset = vec2(texelHeightOffset, texelWidthOffset) * blurSize2;" +

                    "for (lowp int i = 0; i < GAUSSIAN_SAMPLES; i++) {" +
                    "multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));" +
                    // Blur in x (horizontal)
                    "blurStep = float(multiplier) * singleStepOffset;" +
                    "blurCoordinates[i] = vTextureCoord.xy + blurStep;" +
                    "}" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 vTextureCoord;\n" +
                    "const lowp int GAUSSIAN_SAMPLES = 9;" +
                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +
                    "uniform lowp sampler2D sTexture;\n" +
                    "uniform lowp sampler2D oTexture;\n" +

                    "void main() {" +
                    "lowp vec4 textureColor2 = texture2D(sTexture, vTextureCoord);\n" +
                    "lowp vec4 sum = vec4(0.0);" +
                    "sum += texture2D(oTexture, blurCoordinates[0]) * 0.05;" +
                    "sum += texture2D(oTexture, blurCoordinates[1]) * 0.09;" +
                    "sum += texture2D(oTexture, blurCoordinates[2]) * 0.12;" +
                    "sum += texture2D(oTexture, blurCoordinates[3]) * 0.15;" +
                    "sum += texture2D(oTexture, blurCoordinates[4]) * 0.18;" +
                    "sum += texture2D(oTexture, blurCoordinates[5]) * 0.15;" +
                    "sum += texture2D(oTexture, blurCoordinates[6]) * 0.12;" +
                    "sum += texture2D(oTexture, blurCoordinates[7]) * 0.09;" +
                    "sum += texture2D(oTexture, blurCoordinates[8]) * 0.05;" +

//                    "gl_FragColor = sum;" +
                    "gl_FragColor = mix(textureColor2, sum, sum.a);\n" +

//                    "gl_FragColor = vec4(textureColor2.rgb*0.5+textureColor.rgb*0.5,1.0);\n" +

                    "}";

//      "precision mediump float;\n" +
//              "varying vec2 vTextureCoord;\n" +
//              "uniform lowp sampler2D oTexture;\n" +
//              "uniform lowp sampler2D oTexture;\n" +
//              "void main() {\n" +
//              "   lowp vec4 textureColor = texture2D(oTexture, vTextureCoord);\n" +
//              "   lowp vec4 textureColor2 = texture2D(oTexture, vTextureCoord);\n" +
//              "   \n" +
//              "   gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);\n" +
//              "}\n";

    private float texelWidthOffset = 0.01f;
    private float texelHeightOffset = 0.01f;
    private float blurSize2 = 0.2f;

    public GlOverlayGaussianBlurFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public float getTexelWidthOffset() {
        return texelWidthOffset;
    }

    public void setTexelWidthOffset(final float texelWidthOffset) {
        this.texelWidthOffset = texelWidthOffset;
    }

    public float getTexelHeightOffset() {
        return texelHeightOffset;
    }

    public void setTexelHeightOffset(final float texelHeightOffset) {
        this.texelHeightOffset = texelHeightOffset;
    }

    public float getblurSize2() {
        return blurSize2;
    }

    public void setblurSize2(final float blurSize2) {
        this.blurSize2 = blurSize2;
    }

    public void setResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Size(width, height));
    }

    private void createBitmap() {
        releaseBitmap(bitmap);
        releaseBitmap(bitmapBlur);
        bitmap = Bitmap.createBitmap(inputResolution.getWidth(), inputResolution.getHeight(), Bitmap.Config.ARGB_8888);
        bitmapBlur = Bitmap.createBitmap(inputResolution.getWidth(), inputResolution.getHeight(), Bitmap.Config.ARGB_8888);

    }

    @Override
    public void setup() {
        super.setup();// 1
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        createBitmap();
    }


    @Override
    public void onDraw() {
        GLES20.glUniform1f(getHandle("texelWidthOffset"), texelWidthOffset);
        GLES20.glUniform1f(getHandle("texelHeightOffset"), texelHeightOffset);
        GLES20.glUniform1f(getHandle("blurSize2"), blurSize2);

    }

    @Override
    public void onDraw(long time, int width, int height) {
        GLES20.glUniform1f(getHandle("texelWidthOffset"), texelWidthOffset);
        GLES20.glUniform1f(getHandle("texelHeightOffset"), texelHeightOffset);
        GLES20.glUniform1f(getHandle("blurSize2"), blurSize2);

        if (bitmap == null) {
            createBitmap();
        }
        if (bitmap.getWidth() != inputResolution.getWidth() || bitmap.getHeight() != inputResolution.getHeight()) {
            createBitmap();
        }

        bitmap.eraseColor(Color.argb(0, 0, 0, 0));

        Canvas bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.scale(1, -1, bitmapCanvas.getWidth() / 2f, bitmapCanvas.getHeight() / 2f);
        drawCanvas(bitmapCanvas, time, bitmap.getWidth(), bitmap.getHeight());

        int offsetDepthMapTextureUniform = getHandle("oTexture");// 3
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        if (bitmap != null && !bitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }
        GLES20.glUniform1i(offsetDepthMapTextureUniform, 3);
    }

    protected void drawCanvas(Canvas canvas, long time, int width, int height) {

    }


    public static void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

}
