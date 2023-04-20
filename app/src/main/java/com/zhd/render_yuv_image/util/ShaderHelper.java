package com.zhd.render_yuv_image.util;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderHelper {
    private static final String TAG  = "ShaderHelper";
    private static final boolean DEBUG = true;

        public static int buildProgram(String vertexShaderSource , String fragmentShaderSource){
            // Compile the shader
            int vertexShader = compileVertexShader(vertexShaderSource);
            int fragmentShader = compileFragmentShader(fragmentShaderSource);

            // Link them into a shader program.
            int program = linkProgram(vertexShader, fragmentShader);
            if (DEBUG) {
                validateProgram(program);
            }

            return program;
        }

        private static int  compileVertexShader(String shaderCode){
            return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
        }

        private static int compileFragmentShader(String shaderCode) {
            return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
        }

        private static int compileShader(int type, String shaderCode){
            // 1. create shader
            int shaderObjectId = GLES20.glCreateShader(type);
            if (shaderObjectId == 0) {
                if (DEBUG) {
                    Log.w(TAG, "compileShader: Could not create new shader");
                }

                return 0;
            }

            // 2. upload source to shader
            GLES20.glShaderSource(shaderObjectId, shaderCode);

            // 3. compile shader
            GLES20.glCompileShader(shaderObjectId);

            int[] compileStatus = new int[]{0};
            GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (DEBUG) {
                Log.i(TAG, "compileShader: Results of compiling source:\n $shaderCode \n "
                        + GLES20.glGetShaderInfoLog(shaderObjectId));
            }

            if (compileStatus[0] == 0) {
                // If it failed, delete the shader object.
                GLES20.glDeleteShader(shaderObjectId);

                if (DEBUG) {
                    Log.w(TAG, "compileShader: Compilation of shader failed");
                }

                return 0;
            }

            return shaderObjectId;
        }

        private static int linkProgram(int vertexShaderId, int fragmentShaderId){
            // 1. create program
            int programObjectId = GLES20.glCreateProgram();
            if (programObjectId == 0) {
                if (DEBUG) {
                    Log.w(TAG, "linkProgram: Could not create new program");
                }

                return 0;
            }

            GLES20.glAttachShader(programObjectId, vertexShaderId);
            GLES20.glAttachShader(programObjectId, fragmentShaderId);
            GLES20.glLinkProgram(programObjectId);

            int[] linkStatus = new int[]{0};
            GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (DEBUG) {
                Log.i(TAG, "linkProgram: Results of linking program:\n"
                        + GLES20.glGetProgramInfoLog(programObjectId)
                );
            }

            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programObjectId);
                if (DEBUG) {
                    Log.w(TAG, "linkProgram: failed");
                }

                return 0;
            }

            return programObjectId;
        }

        private static Boolean validateProgram(int programObjectId){
            GLES20.glValidateProgram(programObjectId);

            int[] validateStatus = new int[]{0};
            GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
            Log.i(TAG, "validateProgram: Results of validating program: " + validateStatus[0]
                    + "\nLog: " + GLES20.glGetProgramInfoLog(programObjectId));

            return validateStatus[0] != 0;
        }

}
