package com.example.tejk.opengl_fingerpaint;
public class Vector {
    public double x;
    public double y;
    // Constructor methods ....

    public Vector() {
        x = y = 0.0;
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector(MousePoint mousePoint) {
        this.x = mousePoint.getX();
        this.y = mousePoint.getY();
    }
    // Convert vector to a string ...

    public static Vector add(Vector v1, Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y);
    }
    // Compute magnitude of vector ....

    public static Vector sub(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }
    // Sum of two vectors ....

    public static Vector scale(Vector v1, double scaleFactor) {
        return new Vector(v1.x * scaleFactor, v1.y * scaleFactor);
    }
    // Subtract vector v1 from v .....

    public static Vector normalize(Vector v1) {
        Vector v2 = new Vector();
        double length = Math.sqrt(v1.x * v1.x + v1.y * v1.y);
        if (length != 0) {
            v2.x = v1.x / length;
            v2.y = v1.y / length;
        }
        return v2;
    }
    // Scale vector by a constant ...

    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }
    // Normalize a vectors length....

    public double length() {
        return Math.sqrt(x * x + y * y);
    }
    // Dot product of two vectors .....

    public double dotProduct(Vector v1) {
        return this.x * v1.x + this.y * v1.y;
    }
}