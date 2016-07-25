package com.example.tejk.opengl_fingerpaint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by tej on 7/20/16.
 */
public class CustomGLRender implements GLSurfaceView.Renderer {
    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    ConcurrentLinkedQueue<Mesh> mMeshes;
    Bitmap mTexture;

    // Our screenresolution
    float   mScreenWidth = 1280;
    float   mScreenHeight = 768;

    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;

    public CustomGLRender(Context c)
    {
        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;
    }

    public void onPause()
    {
        /* Do stuff to pause the renderer */
    }

    public void onResume()
    {
        /* Do stuff to resume the renderer */
        mLastTime = System.currentTimeMillis();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Get the current time
        long now = System.currentTimeMillis();

        // We should make sure we are valid and sane
        if (mLastTime > now) return;

        // Get the amount of time the last frame took.
        long elapsed = now - mLastTime;

        // Update our example

        // Render our example
        Render(mtrxProjectionAndView);

        // Save the current time to see how long it took <img src="http://androidblog.reindustries.com/wp-includes/images/smilies/icon_smile.gif" alt=":)" class="wp-smiley"> .
        mLastTime = now;

    }

    private void Render(float[] m) {

        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // get handle to vertex shader's vPosition member
//        int mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_SolidColor, "vPosition");
//
//        // Enable generic vertex attribute array
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//
//        // Prepare the triangle coordinate data
//        GLES20.glVertexAttribPointer(mPositionHandle, 3,
//                                     GLES20.GL_FLOAT, false,
//                                     0, vertexBuffer);
//
//        // Get handle to shape's transformation matrix
//        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_SolidßColor, "uMVPMatrix");
//
//        // Apply the projection and view transformation
//        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
//
//        // Draw the triangle
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
//                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
//
//        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(mPositionHandle);

//        Image Portion
        // get handle to vertex shader's vPosition member
        if(mMeshes!=null) {
            for (Mesh mesh : mMeshes) {
                mesh.draw(m);
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        // Clear our matrices
        for(int i=0;i<16;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        int id = mContext.getResources().getIdentifier("drawable/particle", null,
                mContext.getPackageName());

        // Temporary create a bitmap
        mTexture = BitmapFactory.decodeResource(mContext.getResources(), id);
        mMeshes = new ConcurrentLinkedQueue<>();
//        mMesh = new Mesh(mContext,bmp);

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
        GLES20.glEnable (GLES20.GL_BLEND);
        GLES20.glBlendFunc (GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glge

        // Create the shaders
        int vertexShader = CustomShader.loadShader(GLES20.GL_VERTEX_SHADER, CustomShader.vs_SolidColor);
        int fragmentShader = CustomShader.loadShader(GLES20.GL_FRAGMENT_SHADER, CustomShader.fs_SolidColor);

        CustomShader.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(CustomShader.sp_SolidColor, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(CustomShader.sp_SolidColor, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(CustomShader.sp_SolidColor);                  // creates OpenGL ES program executables

        // Set our shader programm
//        GLES20.glUseProgram(CustomShader.sp_SolidColor);

        // Create the shaders, images
        vertexShader = CustomShader.loadShader(GLES20.GL_VERTEX_SHADER,
                                                 CustomShader.vs_Image);
        fragmentShader = CustomShader.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                   CustomShader.fs_Image);

        CustomShader.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(CustomShader.sp_Image, vertexShader);
        GLES20.glAttachShader(CustomShader.sp_Image, fragmentShader);
        GLES20.glLinkProgram(CustomShader.sp_Image);

        // Set our shader programm
        GLES20.glUseProgram(CustomShader.sp_Image);
    }


//    public void TranslateSprite()
//    {
//        vertices = new float[]
//                {image.left, image.top, 0.0f,
//                        image.left, image.bottom, 0.0f,
//                        image.right, image.bottom, 0.0f,
//                        image.right, image.top, 0.0f,
//                };
//        // The vertex buffer.
//        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//        vertexBuffer = bb.asFloatBuffer();
//        vertexBuffer.put(vertices);
//        vertexBuffer.position(0);
//    }

    public void processTouchEvent(MotionEvent event, GLSurfaceView view)
    {
        // Get the half of screen value
        final float x = event.getX();
        final float y = event.getY();

        view.queueEvent(new Runnable() {
            @Override
            public void run() {
                Mesh mesh = new Mesh(mContext,mTexture);
                mesh.setLeft((int)(x - 100));
                mesh.setRight((int)(x + 100));
                mesh.setTop((int)((mScreenHeight - y) + 100));
                mesh.setBottom((int)((mScreenHeight - y) - 100));

                mMeshes.add(mesh);
                if(mMeshes.size() > 150){
                    mMeshes.poll();
                }
            }
        });



//        Log.d("<^>","Image left :"+ image.left+"Image right :"+ image.right+"Image top:"+ image.top+"Image bottom :"+ image.bottom);
//        Log.d("<^>","Mouse x :"+ event.getX()+"Mouse y :"+event.getY());

        // Update the new data.
//        TranslateSprite();
    }

}
