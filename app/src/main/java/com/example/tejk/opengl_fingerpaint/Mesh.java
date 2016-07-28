package com.example.tejk.opengl_fingerpaint;
import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
/**
 * Created by Main on 7/24/2016.
 */
public class Mesh {
    Context mContext;
    // Geometric variables
    private float width;
    private float height;
    private float left;
    private float right;
    private float top;
    private float bottom;
    private int mTextureId;
    private float vertices[];
    private float color[];
    private float uvs[];
    private int indices[];
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private FloatBuffer uvBuffer;
    private IntBuffer drawListBuffer;
    private float lifeCounterAlpha;
    private float lifeCounterSize;

    public Mesh(Context context, int textureId) {
        mContext = context;
        mTextureId = textureId;
        setupTriangle();
        setupImage();
        lifeCounterAlpha = 1;
        lifeCounterSize = 1;
    }

    private void setupTriangle() {
        // We have create the vertices of our view.
        vertices = new float[]
                {
                        left, bottom,
                        left, top,
                        right, bottom,
                        right, top,
                };
        indices = new int[]{0, 1, 2, 3, 0}; // loop in the android official tutorial opengles why different order.
        color = new float[]
                {1f,
                        1f,
                        0f,
                        1f,
                };
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
        ByteBuffer cb = ByteBuffer.allocateDirect(color.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }

    private void setupImage() {
        // Create our UV coordinates.
        uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };
        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    public void draw(float m[]) {
        calcPoints();
        setColorBuffer(1 - (1 - (1 / lifeCounterAlpha)), color[1], color[2], 1/ lifeCounterAlpha);
        int mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_Image, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2,
                                     GLES20.GL_FLOAT, false,
                                     0, vertexBuffer);
        int colorHandle = GLES20.glGetUniformLocation(CustomShader.sp_Image, "a_color");
        GLES20.glUniform4f(colorHandle, color[0], color[1], color[2], color[3]);
        int mTexCoordLoc = GLES20.glGetAttribLocation(CustomShader.sp_Image, "a_texCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                                     false,
                                     0, uvBuffer);
        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_Image, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        int mSamplerLoc = GLES20.glGetUniformLocation(CustomShader.sp_Image, "s_texture");
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);
        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.length, GLES20.GL_UNSIGNED_INT, drawListBuffer);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        lifeCounterAlpha += lifeCounterAlpha /2.5;
        lifeCounterSize+= lifeCounterSize /30;

        left += lifeCounterSize;
        right -= lifeCounterSize;
        bottom += lifeCounterSize;
        top -= lifeCounterSize;
//        Log.d("<^>", left + "," + top + "," + right + "," + bottom);
    }

    private void calcPoints() {
        vertices = new float[]
                {
                        left, top,
                        left, bottom,
                        right, bottom,
                        right, top,
                };
        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    private void setColorBuffer(float R, float G, float B, float A) {
        color = new float[]
                {R,
                        G,
                        B,
                        A,
                };
        ByteBuffer cb = ByteBuffer.allocateDirect(color.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }

    public float getLeft() {
        return left;
    }

    public void setLeft(float value) {
        left = value;
    }

    public float getRight() {
        return right;
    }

    public void setRight(float value) {
        right = value;
    }

    public float getTop() {
        return top;
    }

    public void setTop(float value) {
        top = value;
    }

    public float getBottom() {
        return bottom;
    }

    public void setBottom(float value) {
        bottom = value;
    }
}
