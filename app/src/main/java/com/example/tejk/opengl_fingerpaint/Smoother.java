package com.example.tejk.opengl_fingerpaint;
import android.util.Log;

import java.util.ArrayList;
/**
 * Created by tej on 7/25/16.
 */
public class Smoother {
    public static int iterations = 2;
    public static double simplifyTolerance = 35f;
    private ArrayList<MeshPoint> tmp = new ArrayList<>();

    public static float linearInterpolation(float a, float b, float t) {
        float out = (Math.abs(a - b) * t) + a;
        Log.d("<^>", " " + a + " " + b + " " + t + " out: " + out);
        return out;
    }

    public void resolve(ArrayList<MeshPoint> input, ArrayList<MeshPoint> output) {
        output.clear();
        if (input.size() <= 2) { //simple copy
            output.addAll(input);
            return;
        }
        //simplify with squared tolerance
        if (simplifyTolerance > 0 && input.size() > 3) {
            simplify(input, simplifyTolerance * simplifyTolerance, tmp);
            input = tmp;
        }
        //perform smooth operations
        if (iterations <= 0) { //no smooth, just copy input to output
            output.addAll(input);
        } else if (iterations == 1) { //1 iteration, smooth to output
            smooth(input, output);
        } else { //multiple iterations.. ping-pong between arrays
            int iters = iterations;
            //subsequent iterations
            do {
                smooth(input, output);
                tmp.clear();
                tmp.addAll(output);
                ArrayList<MeshPoint> old = output;
                input = tmp;
                output = old;
            } while (--iters > 0);
        }
    }

    public static void smooth(ArrayList<MeshPoint> input, ArrayList<MeshPoint> output) {
        //expected size
        output.clear();
        output.ensureCapacity(input.size() * 2);
        //first element
        output.add(input.get(0));
        //average elements
        for (int i = 0; i < input.size() - 1; i++) {
            MeshPoint p0 = input.get(i);
            MeshPoint p1 = input.get(i + 1);
            MeshPoint Q = new MeshPoint(0.75f * p0.point.x + 0.25f * p1.point.x,
                                        0.75f * p0.point.y + 0.25f * p1.point.y,
                                        interpolateColor(p0.color, p1.color, 0.25f),
                                        p0.age * 0.75f + p1.age * .25f);
            MeshPoint R = new MeshPoint(0.25f * p0.point.x + 0.75f * p1.point.x,
                                        0.25f * p0.point.y + 0.75f * p1.point.y,
                                        interpolateColor(p0.color, p1.color, 0.75f),
                                        p0.age * 0.25f + p1.age * .75f);
            output.add(Q);
            output.add(R);
        }
        //last element
        output.add(input.get(input.size() - 1));
    }

    public static ColorV4 interpolateColor(ColorV4 a, ColorV4 b, float t) {
        return new ColorV4
                (
                        a.R + (b.R - a.R) * t,
                        a.G + (b.G - a.G) * t,
                        a.B + (b.B - a.B) * t,
                        a.A + (b.A - a.A) * t
                );
    }

    //simple distance-based simplification
    //adapted from simplify.js
    public static void simplify(ArrayList<MeshPoint> points, double sqTolerance, ArrayList<MeshPoint> out) {
        int len = points.size();
        MeshPoint point;
        MeshPoint prevPoint = points.get(0);
        out.clear();
        out.add(prevPoint);
        for (int i = 1; i < len; i++) {
            point = points.get(i);
            if (distSq(point, prevPoint) > sqTolerance) {
                out.add(point);
                prevPoint = point;
            }
        }
//        if (!prevPoint.equals(point)) {
//            out.add(point);
//        }
    }

    public static double distSq(MeshPoint p1, MeshPoint p2) {
        double dx = p1.point.x - p2.point.x, dy = p1.point.y - p2.point.y;
        return dx * dx + dy * dy;
    }

}
