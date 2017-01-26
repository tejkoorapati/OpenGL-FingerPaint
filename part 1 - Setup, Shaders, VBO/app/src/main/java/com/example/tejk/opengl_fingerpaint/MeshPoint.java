package com.example.tejk.opengl_fingerpaint;
/**
 * Created by tej on 8/5/16.
 */
public class MeshPoint {
    public Vector point;
    public ColorV4 color;
    public float age;

    public MeshPoint(Vector point, ColorV4 color, float age) {
        this.point = point;
        this.color = color;
        this.age = age;
    }

    public MeshPoint(double x, double y) {
        this.point = new Vector(x, y);
        this.color = new ColorV4(0, 0, 0, 1);
    }

    public MeshPoint(double x, double y, ColorV4 color, float age) {
        this.point = new Vector(x, y);
        this.color = color;
        this.age = age;
    }

    public float getAgeFade() {
        return 5f / age;
    }

    public void getOlder() {
        age++;
    }
}
