package com.zhd.render_yuv_image.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Util {
    public static  byte[] read(String assetFileName, Context context) throws IOException {
        InputStream inputStream = context.getAssets().open(assetFileName);
//        int length = inputStream.available();
        byte[] buffer = new byte[345600];
        inputStream.read(buffer);
        return buffer;
    }

}
