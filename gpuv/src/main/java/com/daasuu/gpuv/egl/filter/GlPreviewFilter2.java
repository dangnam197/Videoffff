package com.daasuu.gpuv.egl.filter;

import android.opengl.GLES20;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;

public class GlPreviewFilter2 extends GlFilter {

    public static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "uniform float uCRatio;\n" +

                    "const lowp int GAUSSIAN_SAMPLES = 9;" +
                    "uniform highp float texelWidthOffset;" +
                    "uniform highp float texelHeightOffset;" +
                    "uniform highp float blurSize2;\n" +
                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +


                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +

                    "void main() {\n" +
                    "vec4 scaledPos = aPosition;\n" +
                    "scaledPos.x = scaledPos.x * uCRatio;\n" +
                    "gl_Position = uMVPMatrix * scaledPos;\n" +
                    "vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
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
                    "}\n";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +

                    "const lowp int GAUSSIAN_SAMPLES = 9;" +
                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +

                    "uniform lowp sampler2D sTexture2;" +

                    "void main() {" +
                    "lowp vec4 sum = vec4(0.0);" +

                    "sum += texture2D(sTexture2, blurCoordinates[0]) * 0.05;" +
                    "sum += texture2D(sTexture2, blurCoordinates[1]) * 0.09;" +
                    "sum += texture2D(sTexture2, blurCoordinates[2]) * 0.12;" +
                    "sum += texture2D(sTexture2, blurCoordinates[3]) * 0.15;" +
                    "sum += texture2D(sTexture2, blurCoordinates[4]) * 0.18;" +
                    "sum += texture2D(sTexture2, blurCoordinates[5]) * 0.15;" +
                    "sum += texture2D(sTexture2, blurCoordinates[6]) * 0.12;" +
                    "sum += texture2D(sTexture2, blurCoordinates[7]) * 0.09;" +
                    "sum += texture2D(sTexture2, blurCoordinates[8]) * 0.05;" +

                    "gl_FragColor = sum;" +
                    "}";
    private final int texTarget;

    private float texelWidthOffset = 0.01f;
    private float texelHeightOffset = 0.01f;
    private float blurSize = 0.25f;

    public GlPreviewFilter2(final int texTarget) {
        super(VERTEX_SHADER, createFragmentShaderSourceOESIfNeed(texTarget));
        this.texTarget = texTarget;
    }

    private static String createFragmentShaderSourceOESIfNeed(final int texTarget) {
        if (texTarget == GL_TEXTURE_EXTERNAL_OES) {
            return new StringBuilder()
                    .append("#extension GL_OES_EGL_image_external : require\n")
                    .append(FRAGMENT_SHADER.replace("sampler2D", "samplerExternalOES"))
                    .toString();
        }
        return FRAGMENT_SHADER;
    }

    public void draw(final int texName, final float[] mvpMatrix, final float[] stMatrix, final float aspectRatio) {
        useProgram();

        GLES20.glUniform1f(getHandle("texelWidthOffset"), texelWidthOffset);
        GLES20.glUniform1f(getHandle("texelHeightOffset"), texelHeightOffset);
        GLES20.glUniform1f(getHandle("blurSize2"), blurSize);

        GLES20.glUniformMatrix4fv(getHandle("uMVPMatrix"), 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(getHandle("uSTMatrix"), 1, false, stMatrix, 0);
        GLES20.glUniform1f(getHandle("uCRatio"), aspectRatio);

        GLES20.glBindBuffer(GL_ARRAY_BUFFER, getVertexBufferName());
        GLES20.glEnableVertexAttribArray(getHandle("aPosition"));
        GLES20.glVertexAttribPointer(getHandle("aPosition"), VERTICES_DATA_POS_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_POS_OFFSET);
        GLES20.glEnableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glVertexAttribPointer(getHandle("aTextureCoord"), VERTICES_DATA_UV_SIZE, GL_FLOAT, false, VERTICES_DATA_STRIDE_BYTES, VERTICES_DATA_UV_OFFSET);

        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(texTarget, texName);
        GLES20.glUniform1i(getHandle(DEFAULT_UNIFORM_SAMPLER), 0);

        GLES20.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(getHandle("aPosition"));
        GLES20.glDisableVertexAttribArray(getHandle("aTextureCoord"));
        GLES20.glBindBuffer(GL_ARRAY_BUFFER, 0);
        GLES20.glBindTexture(GL_TEXTURE_2D, 0);
    }
}

