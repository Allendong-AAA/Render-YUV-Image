package com.zhd.render_yuv_image.util;

import android.content.Context;

import androidx.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {
    public static String readTextFileFromResource(Context context, int resId) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resId);
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = bufferedReader.readLine();
        while(line!=null){
            result.append(line);
            result.append("\n");
            line = bufferedReader.readLine();
        }
        return result.toString();
    }
}
