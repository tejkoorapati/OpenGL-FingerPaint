package com.example.tejk.opengl_fingerpaint;
import android.opengl.GLES20;
/**
 * Created by tej on 7/20/16.
 */
public class CustomShader {
    //using texture
//    public static final String vs_Image =
//            "uniform mat4 uMVPMatrix;" +
//                    "attribute vec4 vPosition;" +
//                    "attribute vec2 a_texCoord;" +
//                    "uniform vec4 a_color;" +
//                    "varying vec2 v_texCoord;" +
//                    "varying vec4 v_color;" +
//                    "void main() {" +
//                    "  gl_Position = uMVPMatrix * vPosition;" +
//                    "  v_texCoord = a_texCoord;" +
//                    "  v_color = a_color;" +
//                    "}";
//    public static final String fs_Image =
//            "precision mediump float;" +
//                    "varying vec2 v_texCoord;" +
//                    "varying vec4 v_color;" +
//                    "uniform sampler2D s_texture;" +
//                    "void main() {" +
//                    "  gl_FragColor = v_color *texture2D( s_texture, v_texCoord );" +
//                    "}";

    //using flat color;

    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_color;" +
                    "varying vec4 v_color;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_color = a_color;" +
                    "}";
    public static final String fs_Image =
            "precision mediump float;" +
                    "varying vec4 v_color;" +
                    "void main() {" +
                    "  gl_FragColor = v_color;" +
                    "}";

    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    // Program variables
    public static int sp_SolidColor;
    public static int sp_Image;

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
