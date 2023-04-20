package com.zhd.render_yuv_image;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private  GLSurfaceView mGlSurfaceVie;
    private ImageView mImageView;
    private GLSurfaceView mGlSurfaceView;

    private YUVRender mRender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGlSurfaceView  = findViewById(R.id.gl_surface_view);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mRender = new YUVRender(getApplicationContext());
//        mGlSurfaceView.setRenderMode()
        mGlSurfaceView.setRenderer(mRender);

    }
}