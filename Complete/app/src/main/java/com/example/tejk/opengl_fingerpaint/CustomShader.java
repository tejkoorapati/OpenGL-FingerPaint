package com.example.tejk.opengl_fingerpaint;
import android.opengl.GLES20;
/**
 * Created by tej on 7/20/16.
 */
public class CustomShader {
    public static final String vs_mouseSwipe =
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_color;" +
                    "varying vec4 v_color;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_color = a_color;" +
                    "}";
    public static final String fs_mouseSwipe =
                    "precision mediump float;" +
                    "varying vec4 v_color;" +
                    "void main() {" +
                    "  gl_FragColor = v_color;" +
                    "}";
    public static final String vs_Texture =
                    "attribute vec4 inVertex;" +
                    "attribute vec2 inTextureCoordinate;" +
                    "uniform mat4 MVP;" +
                    "varying vec2 textureCoordinate;" +
                    "void main() {" +
                    "gl_Position = MVP * inVertex;" +
                    "textureCoordinate = inTextureCoordinate;" +
                    "}";
    public static final String fs_Texture =
            "precision mediump float;" +
            "varying vec2 textureCoordinate;" +
                    "uniform sampler2D texture;" +
                    "void main() {" +
                    "gl_FragColor = texture2D(texture, textureCoordinate);" +
                    "}";
    public static int sp_mouse_swipe;
    public static int sp_background;

    public static int loadShader(int type, String shaderCode){

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
