package com.example.tejk.opengl_fingerpaint;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 * My name is Tej and I am a poo poo.
 * Created by tej on 8/4/16.
 */
public class SwipeMesh implements IOpenGLObject {
    private final int ageOfDeath = 60;
    Smoother mSmoother = new Smoother();
    CustomGLSurface glSurface;
    private ConcurrentLinkedQueue<MeshPoint> meshPointQueue;
    private ArrayList<MeshPoint> mSegments;
    private float screenHeight;
    private FloatBuffer swipeBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
    private IntBuffer indexBuffer = ByteBuffer.allocateDirect(0).asIntBuffer();
    private FloatBuffer colorBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer();
    private float[] segmentCoords;
    private int[] indices = new int[0];
    private float[] colorArray = new float[0];
    private Timer ageTimer;
    private int removeCounter = 0;

    public SwipeMesh(float screenHeight, CustomGLSurface surface) {
        mSegments = new ArrayList<MeshPoint>();
        meshPointQueue = new ConcurrentLinkedQueue<>();
        this.screenHeight = screenHeight;
        glSurface = surface;
        ageTimer = new Timer();
        ageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (MeshPoint meshPoint : meshPointQueue) {
                    meshPoint.getOlder();
                    removeCounter++;
                    if (meshPoint.age > ageOfDeath) {
                        meshPointQueue.poll();
                        removeCounter = 0;
                    }
                }
                pointCalc();
            }
        }, 0, 1000L / 120L);
    }

    private void pointCalc() {
        glSurface.queueEvent(new Runnable() {
            @Override
            public void run() {
                ArrayList<MeshPoint> out = new ArrayList<>();
                mSegments.clear();
                if (meshPointQueue.size() > 3) {
                    mSmoother.resolve(new ArrayList<>(meshPointQueue), out);
                    Vector A, B;
                    MeshPoint C = null;
                    MeshPoint D = null;
                    for (int i = 0; i < out.size(); i++) {
                        if (i == 0 || i == out.size() - 1) {
                            MeshPoint meshPoint = out.get(i);
                            mSegments.add(new MeshPoint(convertToGLCoords(meshPoint.point), out.get(i).color, meshPoint.age));
                        } else {
                            MeshPoint currentMeshPoint = out.get(i);
                            MeshPoint nextMeshPoint = out.get(i + 1);
                            A = currentMeshPoint.point;
                            B = nextMeshPoint.point;
                            Vector perp = findPerp(A, B);
                            C = new MeshPoint(
                                    convertToGLCoords(Vector.add(B, Vector.scale(perp, 40 * ((float) i / out.size())))),
                                    nextMeshPoint.color,
                                    nextMeshPoint.age);
                            D = new MeshPoint(
                                    convertToGLCoords(Vector.sub(B, Vector.scale(perp, 40 * ((float) i / out.size())))),
                                    nextMeshPoint.color,
                                    nextMeshPoint.age);
                            mSegments.add(C);
                            mSegments.add(D);
                        }
                    }
                    if (meshPointQueue.size() > 50) {
                        meshPointQueue.poll();
                    }
                }
            }
        });
    }

    private Vector convertToGLCoords(Vector in) {
        return new Vector(in.x, screenHeight - in.y);
    }

    private Vector findPerp(Vector A, Vector B) {
        Vector dir = Vector.sub(B, A);
        Vector nDir = Vector.normalize(dir);
        return new Vector(-1 * nDir.y, nDir.x);
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void addPoint(Vector point, ColorV4 color) {
        if (!meshPointQueue.isEmpty()) {
            for (MeshPoint meshPoint : meshPointQueue) {
                meshPoint.age++;
            }
        }
        meshPointQueue.add(new MeshPoint(point, color, 1));
    }

    public void draw(float m[]) {
//        Log.d("gl_swipe", "before");
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe);
        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe);
//        Log.d("gl_swipe","after");
        setupBuffers(mSegments);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        int mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, swipeBuffer);
        int colorHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "a_color");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.length, GLES20.GL_UNSIGNED_INT, indexBuffer);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private void setupBuffers(ArrayList<MeshPoint> segments) {
        float floatArray[] = genVertexArray(segments);
        indices = new int[segments.size()];
        colorArray = new float[segments.size() * 4];
        int j = 0;
        for (int i = 0; i < segments.size(); i++) {
            MeshPoint segment = segments.get(i);
            indices[i] = i;
            colorArray[j] = segment.color.R;
            colorArray[j + 1] = segment.color.G;
            colorArray[j + 2] = segment.color.B;
            colorArray[j + 3] = 1 - (segment.age / ageOfDeath);
            j += 4;
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

    private float[] genVertexArray(ArrayList<MeshPoint> segments) {
        int inSize = segments.size();
        float out[] = new float[inSize * 2];
        if (segments.isEmpty()) return out;
        for (int i = 0; i < inSize; i++) {
            out[2 * i] = (float) segments.get(i).point.x;
            out[2 * i + 1] = (float) segments.get(i).point.y;
        }
        return out;
    }
}
