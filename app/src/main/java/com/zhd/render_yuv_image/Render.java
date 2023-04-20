package com.zhd.render_yuv_image;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.zhd.render_yuv_image.util.ShaderHelper;
import com.zhd.render_yuv_image.util.TextResourceReader;
import com.zhd.render_yuv_image.util.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Render implements GLSurfaceView.Renderer{

    private static final String TAG = "Render";
    private static final String A_POSITION = "aPosition";
    private static final String A_TEXTURE_COORD = "aTextureCoord";
    private static final String U_PROJECT_MATRIX = "uProjectMatrix";
    private static final String U_VIEW_MATRIX = "uViewMatrix";
    private static final String U_MODEL_MATRIX = "uModelMatrix";
    private static final String U_TEXTURE = "uTexture2D";

    private static final float[] VERTEX = new float[]{
                -0.5f, -0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 1.0f, 1.0f,
                -0.5f,  0.5f, 0.0f, 0.0f,
                0.5f,  0.5f, 1.0f, 0.0f
    };

    private Context mContext;
    private FloatBuffer mFloatBuffer = null;

    private float[] mModelMatrix = new float[]{16};
    private float[] mViewMatrix = new float[]{16};
    private float[] mProjectMatrix = new float[]{16};

    private int mProgram = -1;
    private int mTextureId = -1;

    private int mPositionLoc = -1;
    private int mTextureCoord = -1;
    private int mProjectMatrixLoc = -1;
    private int mViewMatrixLoc = -1;
    private int mModelMatrixLoc = -1;
    private int mTextureLoc = -1;

    public Render(Context context){
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        try {
            mProgram = ShaderHelper.buildProgram(
                    TextResourceReader.readTextFileFromResource(mContext, R.raw.vertex),
                    TextResourceReader.readTextFileFromResource(mContext, R.raw.fragment)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        mTextureCoord = GLES20.glGetAttribLocation(mProgram, A_TEXTURE_COORD);
        mProjectMatrixLoc = GLES20.glGetUniformLocation(mProgram, U_PROJECT_MATRIX);
        mViewMatrixLoc = GLES20.glGetUniformLocation(mProgram, U_VIEW_MATRIX);
        mModelMatrixLoc = GLES20.glGetUniformLocation(mProgram, U_MODEL_MATRIX);
        mTextureLoc = GLES20.glGetUniformLocation(mProgram, U_TEXTURE);

        mFloatBuffer = ByteBuffer
                .allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mFloatBuffer.put(VERTEX);

        mTextureId = TextureHelper.loadTexture(mContext, R.drawable.ic_launcher);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: width = $width, height = $height");
        GLES20.glViewport(0, 0, width, height);

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.setLookAtM(mViewMatrix, 0,
                0F, 0F, 7F,
                0F, 0F, 0F,
                0F, 1F, 0F);

        float ratio = 1F * width / height;
        Matrix.frustumM(mProjectMatrix, 0,
                -ratio, ratio, -1F, 1F, 3F, 7F);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 默认逆时针为正面
        GLES20.glFrontFace(GLES20.GL_CCW);
        // 打开背面裁剪功能;
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        // 清除颜色和深度缓冲;
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glUseProgram(mProgram);
        mFloatBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        GLES20.glVertexAttribPointer(
            mPositionLoc, 2, GLES20.GL_FLOAT, false, 16, mFloatBuffer
        );
        mFloatBuffer.position(2);
        GLES20.glEnableVertexAttribArray(mTextureCoord);
        GLES20.glVertexAttribPointer(
        mTextureCoord, 2, GLES20.GL_FLOAT, false, 16, mFloatBuffer
        );

        GLES20.glUniformMatrix4fv(mProjectMatrixLoc, 1, false, mProjectMatrix, 0);
        GLES20.glUniformMatrix4fv(mViewMatrixLoc, 1, false, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(mModelMatrixLoc, 1, false, mModelMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(mTextureLoc, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTextureCoord);
    }
}
