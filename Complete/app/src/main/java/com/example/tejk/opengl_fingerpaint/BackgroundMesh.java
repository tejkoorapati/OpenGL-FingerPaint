package com.example.tejk.opengl_fingerpaint;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Timer;
/**
 * Created by tej on 8/16/16.
 */
public class BackgroundMesh implements IOpenGLObject {
    public ColorV4 currentColor;
    private ArrayList<ColorV4> backgroundColors;
    private Timer colorTimer;
    private int startColor;
    private int endColor;
    private float interpolatePercent;
    private int textureIDs[];
    private Bitmap texture;
    private float vertices[];
    private float uvs[];
    private int indices[];
    private FloatBuffer uvBuffer;
    private FloatBuffer vertexBuffer;
    private IntBuffer drawListBuffer;
    private float screenWidth;
    private float screenHeight;
    public BackgroundMesh(float screenHeight, float screenWidth, Bitmap texture) {
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.texture = texture;
        colorTimer = new Timer();
        startColor = 0;
        endColor = 1;
        interpolatePercent = 0.0f;
        backgroundColors = new ArrayList<>();
        backgroundColors.add(new ColorV4(0.792f, 1f, 0.952f, 1)); //blue
        backgroundColors.add(new ColorV4(0.870f, 0.741f, 1, 1)); //purple
        backgroundColors.add(new ColorV4(1f, 0.615f, 0.819f, 1)); //pink
        this.currentColor = new ColorV4(0.792f, 1f, 0.952f, 1); //default blue start
        init();
//        colorTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if(interpolatePercent >= 1){
//                    startColor = ( startColor + 1 ) % 3;
//                    endColor = ( endColor + 1 ) % 3;
//                    interpolatePercent = 0;
//                }
//                currentColor=  GLUtils.interpolateColor(backgroundColors.get(startColor),
//                                         backgroundColors.get(endColor),
//                                         interpolatePercent);
//                interpolatePercent += 0.01f;
//
//            }
//        }, 0, 1000L / 30L);

    }

    private void init(){
        setupImage();
        initTextures();
    }

    private void initTextures() {
        textureIDs = new int[1];
        GLES20.glGenTextures(1, textureIDs, 0);
        // Retrieve our image from resources.
        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIDs[0]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                               GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                               GLES20.GL_LINEAR);
        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                               GLES20.GL_CLAMP_TO_EDGE);
        // Load the bitmap into the bound texture.
        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
        texture.recycle();
    }
    private void setupImage() {
        // Create our UV coordinates.

        Log.d("image width", ""+ texture.getWidth());
        Log.d("screen width", ""+ screenWidth);
        Log.d("image height", ""+ texture.getHeight());
        Log.d("screen height", ""+ screenHeight);
        float ratio = 0;
        if (texture.getWidth() > screenWidth){
            ratio = screenWidth/texture.getWidth();
        }

        uvs = new float[]{
                ratio, 0.5f, //bottom left
                ratio, 1.0f, //top left
                1.0f - ratio, 0.5f, //bottom right
                1.0f - ratio, 1f //top right
        };
        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

    }

    @Override

    public void draw(float[] m) {


//        Log.d("gl_background","before");
        GLES20.glUseProgram(CustomShader.sp_background);
//        Log.d("gl_background","after");
        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_background, "MVP");
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        GLES20.glClearColor(currentColor.R, currentColor.G, currentColor.B, 1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        int mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_background, "inVertex");
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 2,
                                     GLES20.GL_FLOAT, false,
                                     0, vertexBuffer);

        int mTexCoordLoc = GLES20.glGetAttribLocation(CustomShader.sp_background, "inTextureCoordinate");
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                                     false,
                                     0, uvBuffer);
        // Get handle to shape's transformation matrix‘


        // Get handle to textures locations

        int mSamplerLoc = GLES20.glGetUniformLocation(CustomShader.sp_background, "texture");

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);
        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.length,
                              GLES20.GL_UNSIGNED_INT, drawListBuffer);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
        createBackgroundMesh();
    }

    private void createBackgroundMesh() {
        // We have create the vertices of our view.
        vertices = new float[]
                {0,0,
                 0, screenHeight,
                 screenWidth,0,
                 screenWidth,screenHeight
                };
        indices = new int[]{0, 1, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 4);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asIntBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
        createBackgroundMesh();
    }
}
