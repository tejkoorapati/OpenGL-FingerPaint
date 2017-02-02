package com.example.tejk.opengl_fingerpaint;
import android.opengl.GLES20;
/**
 * Created by tej on 7/20/16.
 */
public class CustomShader {
    //Shader code goes here:

    public static int sp_mouse_swipe;
    public static int sp_background;

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        // return the shader
        return shader;
    }
}
