package com.example.tejk.opengl_fingerpaint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
    ConcurrentLinkedQueue<Mesh> mMeshes;
    ConcurrentLinkedQueue<MousePoint> mMousePoints;
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
    private ArrayList<Vector> mSegment;
    private int[] textureIDs = new int[1];
    private FloatBuffer swipeBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
    private IntBuffer indexBuffer =  ByteBuffer.allocateDirect(0).asIntBuffer();;
    private FloatBuffer colorBuffer =  ByteBuffer.allocateDirect(0).asFloatBuffer();;
    private float[] segmentCoords;
    private int[] indices = new int[0];
    private float[] colorArray = new    float[0];

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
        int mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_Image, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2,GLES20.GL_FLOAT, false, 0, swipeBuffer);
        int colorHandle = GLES20.glGetAttribLocation(CustomShader.sp_Image, "a_color");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,GLES20.GL_FLOAT, false, 0, colorBuffer);

//        GLES20.glUniform4f(colorHandle, 1, 0, 0, 1);
        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_Image, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.length, GLES20.GL_UNSIGNED_INT, indexBuffer);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);


/*
        if (mMeshes != null && !mMeshes.isEmpty()) {
            for (Mesh mesh : mMeshes) {
                if (mesh.getTop() - mesh.getBottom() < 30 || mesh.getRight() - mesh.getLeft() < 30) {
                    mMeshes.poll();
                    continue;
                }
                mesh.draw(m);
            }
        }
        */
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int id = mContext.getResources().getIdentifier("drawable/particle_solid", null,
                                                       mContext.getPackageName());
        // Temporary create a bitmap
        mTexture = BitmapFactory.decodeResource(mContext.getResources(), id);
//        initTextures();
        mMeshes = new ConcurrentLinkedQueue<>();
        mSegment = new ArrayList<>();
        mMousePoints = new ConcurrentLinkedQueue<>();
//        mMesh = new Mesh(mContext,bmp);
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
//        GLES20.glEnable(GLES20.GL_ALPHA_TEST);
//        GLES20.glge
        // Create the shaders
        // Set our shader programm
//        GLES20.glUseProgram(CustomShader.sp_SolidColor);
        // Create the shaders, images
        int vertexShader = CustomShader.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   CustomShader.vs_Image);
        int fragmentShader = CustomShader.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     CustomShader.fs_Image);
        CustomShader.sp_Image = GLES20.glCreateProgram();
        GLES20.glAttachShader(CustomShader.sp_Image, vertexShader);
        GLES20.glAttachShader(CustomShader.sp_Image, fragmentShader);
        GLES20.glLinkProgram(CustomShader.sp_Image);
        // Set our shader programm
        GLES20.glUseProgram(CustomShader.sp_Image);
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
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mTexture, 0);
    }

    public void processTouchEvent(MotionEvent event) {
        // Get the half of screen value
        final float x = event.getX();
        final float y = event.getY();
        float interpolatedX;
        float interpolatedY;
        MousePoint mousePoint = new MousePoint(x, y);
        ArrayList<MousePoint> out = new ArrayList<>();
        mMousePoints.add(mousePoint);
        if (mMousePoints.size() > 3) {
            mSegment.clear();
            mSmoother.resolve(new ArrayList<>(mMousePoints), out);
//            Log.d("<^>","New size:" + out.size());
//            addMesh(out);
            Vector A, B;
            Vector C = null;
            Vector D = null;
            for (int i = 0; i < out.size(); i++) {
                if (i == 0 || i == out.size() - 1) {
                    mSegment.add(convertToGLCoords(new Vector(out.get(i))));
                } else {
                    A = new Vector(out.get(i));
                    B = new Vector(out.get(i + 1));
                    Vector perp = findPerp(A, B);
                    C = convertToGLCoords(Vector.add(B, Vector.scale(perp, 20 * ((float) i / out.size()))));
                    D = convertToGLCoords(Vector.sub(B, Vector.scale(perp, 20 * ((float) i / out.size()))));
                    mSegment.add(C);
                    mSegment.add(D);
                }
            }
            if (mMousePoints.size() > 50) {
                mMousePoints.poll();
            }
//            mMousePoints.clear();
//            mMousePoints.add(mousePoint);
        }
        mSurface.queueEvent(new Runnable() {
            @Override
            public void run() {
                setupBuffers();
            }
        });

//        addMesh(x,y);
    }

    private Vector findPerp(Vector A, Vector B) {
        Vector dir = Vector.sub(B, A);
        Vector nDir = Vector.normalize(dir);
        return new Vector(-1 * nDir.y, nDir.x);
    }

    private void setupBuffers() {
        float floatArray[] = Vector2DListToArray(mSegment);
        indices = new int[floatArray.length / 2];
        colorArray = new float[floatArray.length * 2];
        int j = 0;
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
            colorArray[j] = (float)i / indices.length;
            colorArray[j+ 1] = 0;
            colorArray[j + 2] = 0;
            colorArray[j + 3] = 1;
            j+=4;
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(floatArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        swipeBuffer = bb.asFloatBuffer();
        swipeBuffer.put(floatArray);
        swipeBuffer.position(0);
        ByteBuffer cb = ByteBuffer.allocateDirect(colorArray.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colorArray);
        colorBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 4);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asIntBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    private float[] Vector2DListToArray(ArrayList<Vector> in) {
        ArrayList<Vector> inCopy = (ArrayList<Vector>) in.clone();
        int inSize = inCopy.size();
        float out[] = new float[inSize * 2];
        for (int i = 0; i < inSize; i++) {
            out[2 * i] = (float) inCopy.get(i).x;
            out[2 * i + 1] = (float) inCopy.get(i).y;
        }
        return out;
    }

    private Vector convertToGLCoords(Vector in) {
        return new Vector(in.x, mScreenHeight - in.y);
    }

    private void addMesh(final ArrayList<MousePoint> mousePoints) {
        mSurface.queueEvent(new Runnable() {
            @Override
            public void run() {
                for (MousePoint mousePoint : mousePoints) {
                    float x = mousePoint.getX();
                    float y = mousePoint.getY();
                    Mesh mesh = new Mesh(mContext, textureIDs[0]);
                    mesh.setLeft((x - 50));
                    mesh.setRight((x + 50));
                    mesh.setTop(((mScreenHeight - y) + 50));
                    mesh.setBottom(((mScreenHeight - y) - 50));
                    mMeshes.add(mesh);
                    if (mMeshes.size() > 300) {
                        mMeshes.poll();
                    }
                }
            }
        });
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
