package com.example.tejk.opengl_fingerpaint;
import java.util.ArrayList;
/**
 * Created by tej on 7/25/16.
 */
public class Smoother {
    public static int iterations = 2;
    public static float simplifyTolerance = 35f;
    private ArrayList<MousePoint> tmp = new ArrayList<>();

    public void resolve(ArrayList<MousePoint> input, ArrayList<MousePoint> output) {
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
                ArrayList<MousePoint> old = output;
                input = tmp;
                output = old;
            } while (--iters > 0);
        }
    }

    public static void smooth(ArrayList<MousePoint> input, ArrayList<MousePoint> output) {
        //expected size
        output.clear();
        output.ensureCapacity(input.size() * 2);
        //first element
        output.add(input.get(0));
        //average elements
        for (int i = 0; i < input.size() - 1; i++) {
            MousePoint p0 = input.get(i);
            MousePoint p1 = input.get(i + 1);
            MousePoint Q = new MousePoint(0.75f * p0.getX() + 0.25f * p1.getX(), 0.75f * p0.getY() + 0.25f * p1.getY());
            MousePoint R = new MousePoint(0.25f * p0.getX() + 0.75f * p1.getX(), 0.25f * p0.getY() + 0.75f * p1.getY());
            output.add(Q);
            output.add(R);
        }
        //last element
        output.add(input.get(input.size() - 1));
    }

    //simple distance-based simplification
    //adapted from simplify.js
    public static void simplify(ArrayList<MousePoint> points, float sqTolerance, ArrayList<MousePoint> out) {
        int len = points.size();
        MousePoint point = new MousePoint(0, 0);
        MousePoint prevPoint = points.get(0);
        out.clear();
        out.add(prevPoint);
        for (int i = 1; i < len; i++) {
            point = points.get(i);
            if (distSq(point, prevPoint) > sqTolerance) {
                out.add(point);
                prevPoint = point;
            }
        }
        if (!prevPoint.equals(point)) {
            out.add(point);
        }
    }

    public static float distSq(MousePoint p1, MousePoint p2) {
        float dx = p1.getX() - p2.getX(), dy = p1.getY() - p2.getY();
        return dx * dx + dy * dy;
    }
}
