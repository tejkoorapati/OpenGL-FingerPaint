package com.example.tejk.opengl_fingerpaint;
import android.graphics.Bitmap;
import android.util.Log;

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
    }

    private void init() {
        setupImage();
        initTextures();
    }

    private void initTextures() {
    }

    private void setupImage() {
        // Create our UV coordinates.
        Log.d("image width", "" + texture.getWidth());
        Log.d("screen width", "" + screenWidth);
        Log.d("image height", "" + texture.getHeight());
        Log.d("screen height", "" + screenHeight);
    }

    @Override
    public void draw(float[] m) {
    }

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
        createBackgroundMesh();
    }

    private void createBackgroundMesh() {
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
        createBackgroundMesh();
    }
}
