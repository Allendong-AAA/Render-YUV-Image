package com.zhd.render_yuv_image;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.zhd.render_yuv_image.util.ShaderHelper;
import com.zhd.render_yuv_image.util.TextResourceReader;
import com.zhd.render_yuv_image.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class YUVRender implements GLSurfaceView.Renderer{

    public YUVRender(Context context){
        this.context = context;
    }

    private static final String TAG = "YUVRender";
    private static final int YUV_TYPE = 2;
    private static final float[] VERTEXT = new float[]{
            -1f, -1f, 0.0f, 1.0f,
            1f, -1f, 1.0f, 1.0f,
            -1f,  1f, 0.0f, 0.0f,
            1f,  1f, 1.0f, 0.0f
    };

    private Context context;

    private int program = -1;
    private int positionLoc = -1;
    private int texCoordLoc = -1;
    private int yTextureLoc = -1;
    private int uTextureLoc = -1;
    private int vTextureLoc = -1;
    private int uvTextureLoc = -1;
    private int matrixLoc = -1;
    private FloatBuffer vertexBuffer = null;
    private float[] matrix = new float[16];
    private final int imageWidth = 640;
    private final int imageHeight = 360;
    private int yTextureId = -1;
    private int uTextureId = -1;
    private int vTextureId = -1;
    private int uvTextureId = -1;
    private int typeLoc = -1;

    private byte[] imageBytes = new byte[345600];
    private ByteBuffer yBuffer;
    private ByteBuffer uBuffer;
    private ByteBuffer vBuffer;
    private ByteBuffer uvBuffer;

    private InputStream inputStream;



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        try {
            program = ShaderHelper.buildProgram(
                    TextResourceReader.readTextFileFromResource(context, R.raw.vertex_yuv),
                    TextResourceReader.readTextFileFromResource(context, R.raw.fragment_yuv)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        positionLoc = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordLoc = GLES20.glGetAttribLocation(program, "aTexCoord");
        yTextureLoc = GLES20.glGetUniformLocation(program, "yTexture");
        uTextureLoc = GLES20.glGetUniformLocation(program, "uTexture");
        vTextureLoc = GLES20.glGetUniformLocation(program, "vTexture");
        uvTextureLoc = GLES20.glGetUniformLocation(program, "uvTexture");
        matrixLoc = GLES20.glGetUniformLocation(program, "matrix");
        typeLoc = GLES20.glGetUniformLocation(program, "type");

        vertexBuffer = ByteBuffer
                .allocateDirect(VERTEXT.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(VERTEXT);

        try {
            inputStream = context.getAssets().open("files_onMediaPlayerVideoFrame_640_360_0_I420.yuv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onSurfaceCreated: inputStream init");

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: width = $width, height = $height");
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(matrix, 0);
        float sx = 1f * imageWidth / width;
        float sy = 1f * imageHeight / height;
        Matrix.scaleM(matrix, 0, sx, sy, 1f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            inputStream.read(imageBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        yBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight)
                .order(ByteOrder.nativeOrder());
        yBuffer.put(imageBytes, 0, imageWidth * imageHeight);
        yBuffer.position(0);

        uBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight / 4)
                .order(ByteOrder.nativeOrder());
        uBuffer.put(imageBytes, imageWidth * imageHeight, imageWidth * imageHeight / 4);
        uBuffer.position(0);

        vBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight / 4)
                .order(ByteOrder.nativeOrder());
        vBuffer.put(imageBytes, imageWidth * imageHeight * 5 / 4, imageWidth * imageHeight / 4);
        vBuffer.position(0);

        int[] textureObjectIds = new int[3];
        GLES20.glGenTextures(3, textureObjectIds, 0);

        // y texture
        yTextureId = textureObjectIds[0];
        textureLuminance(yBuffer, imageWidth, imageHeight, yTextureId);

        // u texture
        uTextureId = textureObjectIds[1];
        textureLuminance(uBuffer, imageWidth / 2, imageHeight / 2, uTextureId);

        // v texture
        vTextureId = textureObjectIds[2];
        textureLuminance(vBuffer, imageWidth / 2, imageHeight / 2, vTextureId);


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(program);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(
                positionLoc, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer
        );

        vertexBuffer.position(2);
        GLES20.glEnableVertexAttribArray(texCoordLoc);
        GLES20.glVertexAttribPointer(
                texCoordLoc, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer
        );

        GLES20.glUniformMatrix4fv(matrixLoc, 1, false, matrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTextureId);
        GLES20.glUniform1i(yTextureLoc, 0);

        GLES20.glUniform1i(typeLoc, YUV_TYPE);

        if (YUV_TYPE == 1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uvTextureId);
            GLES20.glUniform1i(uvTextureLoc, 1);
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uTextureId);
            GLES20.glUniform1i(uTextureLoc, 1);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vTextureId);
            GLES20.glUniform1i(vTextureLoc, 2);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(positionLoc);
        GLES20.glDisableVertexAttribArray(texCoordLoc);
    }

    private void textureLuminance(ByteBuffer imageData, int width, int height, int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_LUMINANCE, width, height, 0,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, imageData
        );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void textureLuminanceAlpha(ByteBuffer imageData, int width, int height, int textureId) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
                GLES20.GL_LUMINANCE_ALPHA,
                GLES20.GL_UNSIGNED_BYTE, imageData
        );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

}
