package com.example.tejk.opengl_fingerpaint;
/**
 * Created by tej on 7/25/16.
 */
public class MousePoint {
    private float x;
    private float y;

    public MousePoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
