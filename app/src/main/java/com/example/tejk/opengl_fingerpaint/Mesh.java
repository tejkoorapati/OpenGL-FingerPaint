package com.example.tejk.opengl_fingerpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Main on 7/24/2016.
 */

public class Mesh {

    Context mContext;

    // Geometric variables
    private Rect image;
    private Bitmap mTexture;
    private float vertices[];
    private float uvs[];
    private int indices[];
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;
    private IntBuffer drawListBuffer;

    public Mesh(Context context, Bitmap texture) {
        mContext = context;
        mTexture = texture;
        setupTriangle();
        setupImage();
    }

    private void setupTriangle() {

        // Initial rect
        image = new Rect();
        image.left = 10;
        image.right = 100;
        image.bottom = 100;
        image.top = 200;


        // We have create the vertices of our view.
        vertices = new float[]
                {image.left, image.top, 0.0f,
                        image.left, image.bottom, 0.0f,
                        image.right, image.bottom, 0.0f,
                        image.right, image.top, 0.0f,
                };

        indices = new int[]{0, 1, 2, 0, 2, 3}; // loop in the android official tutorial opengles why different order.

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

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        // Retrieve our image from resources.


        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

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


        // We are done using the bitmap so we should recycle it.
//        bmp.recycle();

    }


    public void draw(float m[]) {
        calcPoints();
        int mPositionHandle =
                GLES20.glGetAttribLocation(CustomShader.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(CustomShader.sp_Image,
                "a_texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_Image,
                "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(CustomShader.sp_Image,
                "s_texture");

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.length,
                GLES20.GL_UNSIGNED_INT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }

    private void calcPoints(){
        vertices = new float[]
                {image.left, image.top, 0.0f,
                        image.left, image.bottom, 0.0f,
                        image.right, image.bottom, 0.0f,
                        image.right, image.top, 0.0f,
                };
        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void setRight(int value) {
        image.right = value;
    }

    public void setTop(int value) {
        image.top = value;
    }

    public void setLeft(int value) {
        image.left = value;
    }

    public void setBottom(int value) {
        image.bottom = value;
    }

}
