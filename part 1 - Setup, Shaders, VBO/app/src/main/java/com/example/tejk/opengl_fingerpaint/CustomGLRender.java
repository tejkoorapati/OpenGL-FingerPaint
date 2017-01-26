package com.example.tejk.opengl_fingerpaint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
/**
 * Created by tej on 7/20/16.
 */
public class CustomGLRender implements GLSurfaceView.Renderer {
    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];
    private final float colorIntensityArray[] = {
            0.345098f, 0.172549f, 0.513725f,
            0.380915f, 0.161046f, 0.519216f,
            0.416732f, 0.149542f, 0.524706f,
            0.452549f, 0.138039f, 0.530196f,
            0.488366f, 0.126536f, 0.535686f,
            0.524183f, 0.115033f, 0.541176f,
            0.560000f, 0.103529f, 0.546667f,
            0.595817f, 0.092026f, 0.552157f,
            0.631634f, 0.080523f, 0.557647f,
            0.667451f, 0.069020f, 0.563137f,
            0.703268f, 0.057516f, 0.568627f,
            0.739085f, 0.046013f, 0.574118f,
            0.774902f, 0.034510f, 0.579608f,
            0.810719f, 0.023007f, 0.585098f,
            0.846536f, 0.011503f, 0.590588f,
            0.882353f, 0.000000f, 0.596078f};
    ConcurrentLinkedQueue<Mesh> mMeshes;
    ConcurrentLinkedQueue<MousePoint> mMousePoints;
    ArrayList<IOpenGLObject> openGLObjects;
    float[] mousePoints;
    Bitmap mTexture;
    Smoother mSmoother = new Smoother();
    // Our screenresolution
    float mScreenWidth = 1280;
    float mScreenHeight = 768;
    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;
    CustomGLSurface mSurface;
    private SwipeMesh swipeMesh1;
    private SwipeMesh swipeMesh2;
    private BackgroundMesh backgroundMesh;
    private Vector prevPointer1Point;
    private Vector prevPointer2Point;
    private float screenhypotenuse;
    private boolean surfaceLoaded;

    public CustomGLRender(Context c, CustomGLSurface surface) {
        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;
        mSurface = surface;
    }

    public static float InterpolateHermite4pt3oX(float x0, float x1, float x2, float x3, float t) {
        Log.d("<^>", String.valueOf(t));
        float c0 = x1;
        float c1 = .5F * (x2 - x0);
        float c2 = x0 - (2.5F * x1) + (2 * x2) - (.5F * x3);
        float c3 = (.5F * (x3 - x0)) + (1.5F * (x1 - x2));
        return (((((c3 * t) + c2) * t) + c1) * t) + c0;
    }

    public void onPause() {
        /* Do stuff to pause the renderer */
    }

    public void onResume() {
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
        for (IOpenGLObject openGLObject : openGLObjects) {
            openGLObject.draw(m);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // We need to know the current width and height.
        Log.d("<^>", "Surface Changed start");
        surfaceLoaded = false;
        mScreenWidth = width;
        mScreenHeight = height;
        screenhypotenuse = (float) Math.hypot(mScreenWidth, mScreenHeight);
        swipeMesh1.setScreenHeight(height);
        swipeMesh2.setScreenHeight(height);
        backgroundMesh.setScreenHeight(mScreenHeight);
        backgroundMesh.setScreenWidth(mScreenWidth);
        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int) mScreenWidth, (int) mScreenHeight);
        // Clear our matrices
        for (int i = 0; i < 16; i++) {
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
        mousePoints = new float[4];
        surfaceLoaded = true;
        Log.d("<^>", "Surface Changed end");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("<^>", "Surface Create started");
        surfaceLoaded = false;
        int id = mContext.getResources().getIdentifier("drawable/touch_gradient", null,
                                                       mContext.getPackageName());
        // Temporary create a bitmap
        if (mTexture == null) {
            mTexture = BitmapFactory.decodeResource(mContext.getResources(), id);
        }
//        initTextures();
        mMeshes = new ConcurrentLinkedQueue<>();
        backgroundMesh = new BackgroundMesh(mScreenHeight, mScreenWidth, mTexture);
        swipeMesh1 = new SwipeMesh(mScreenHeight, mSurface);
        swipeMesh2 = new SwipeMesh(mScreenHeight, mSurface);
        mMousePoints = new ConcurrentLinkedQueue<>();
        GLES20.glClearColor(1f, 1f, 1f, 1);
        CustomShader.sp_mouse_swipe = GLES20.glCreateProgram();
        int mouseMeshVertexShader = CustomShader.loadShader(GLES20.GL_VERTEX_SHADER,
                                                            CustomShader.vs_mouseSwipe);
        int mouseMeshFragmentShader = CustomShader.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                              CustomShader.fs_mouseSwipe);
        GLES20.glAttachShader(CustomShader.sp_mouse_swipe, mouseMeshVertexShader);
        GLES20.glAttachShader(CustomShader.sp_mouse_swipe, mouseMeshFragmentShader);
        GLES20.glLinkProgram(CustomShader.sp_mouse_swipe);
        CustomShader.sp_background = GLES20.glCreateProgram();
        int backgroundVertexShader = CustomShader.loadShader(GLES20.GL_VERTEX_SHADER,
                                                             CustomShader.vs_Texture);
        int backgroundFragmentShader = CustomShader.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                               CustomShader.fs_Texture);
        GLES20.glAttachShader(CustomShader.sp_background, backgroundVertexShader);
        GLES20.glAttachShader(CustomShader.sp_background, backgroundFragmentShader);
        GLES20.glLinkProgram(CustomShader.sp_background);
        // Set our shader programm
        openGLObjects = new ArrayList<>();
        openGLObjects.add(backgroundMesh);
        openGLObjects.add(swipeMesh1);
        openGLObjects.add(swipeMesh2);
        surfaceLoaded = true;
        Log.d("<^>", "Surface Created ended");
    }

    public void processTouchEvent(MotionEvent event) {
        if (!surfaceLoaded) return;
        int pointerIndex;
        if (event.getPointerCount() <= 2) {
            if (event.findPointerIndex(0) != -1) {
                pointerIndex = event.findPointerIndex(0);
                Vector point = new Vector(event.getX(pointerIndex), event.getY(pointerIndex));
                if (prevPointer1Point == null) {
                    swipeMesh1.addPoint(point, new ColorV4(1, 0, 0, 1));
                } else {
                    swipeMesh1.addPoint(point,
                                        getDistanceColor(prevPointer1Point, point));
                }
                prevPointer1Point = point;
            }
            if (event.findPointerIndex(1) != -1) {
                pointerIndex = event.findPointerIndex(1);
                Vector point = new Vector(event.getX(pointerIndex), event.getY(pointerIndex));
                if (prevPointer2Point == null) {
                    swipeMesh2.addPoint(point, new ColorV4(1, 0, 0, 1));
                } else {
                    swipeMesh2.addPoint(point,
                                        getDistanceColor(prevPointer2Point, point));
                }
                prevPointer2Point = point;
            }
        }
    }

    public ColorV4 getDistanceColor(Vector p1, Vector p2) {
        double width = Math.abs(p1.x - p2.x);
        double height = Math.abs(p1.y - p2.y);
        double distance = Math.hypot(width, height);
        double relativeDistance = screenhypotenuse / 10;
        int index;
        if (distance > relativeDistance) {
            index = colorIntensityArray.length - 3;
        } else {
            index = (int) Math.floor(
                    (distance * (colorIntensityArray.length / 3)) / relativeDistance
                                    ) * 3;
        }
        return new ColorV4(colorIntensityArray[index], colorIntensityArray[index + 1], colorIntensityArray[index + 2], 1f);
    }

    private Vector findPerp(Vector A, Vector B) {
        Vector dir = Vector.sub(B, A);
        Vector nDir = Vector.normalize(dir);
        return new Vector(-1 * nDir.y, nDir.x);
    }

    /*
   Tension: 1 is high, 0 normal, -1 is low
   Bias: 0 is even,
         positive is towards first segment,
         negative towards the other
*/
    float hermiteInterpolate(
            float pointA, float pointB,
            float pointC, float pointD,
            float mu) {
        float m0, m1, mu2, mu3;
        float a0, a1, a2, a3;
        mu2 = mu * mu;
        mu3 = mu2 * mu;
        m0 = (pointB - pointA) * 1 / 2;
        m0 += (pointC - pointB) * 1 / 2;
        m1 = (pointC - pointB) * 1 / 2;
        m1 += (pointD - pointC) * 1 / 2;
        a0 = 2 * mu3 - 3 * mu2 + 1;
        a1 = mu3 - 2 * mu2 + mu;
        a2 = mu3 - mu2;
        a3 = -2 * mu3 + 3 * mu2;
        return (a0 * pointB + a1 * m0 + a2 * m1 + a3 * pointC);
    }
}
