package com.example.tejk.opengl_fingerpaint;
/**
 * Created by tej on 8/16/16.
 */
public class GLUtils {
    public static ColorV4 interpolateColor(ColorV4 a, ColorV4 b, float t) {
        return new ColorV4
                (
                        a.R + (b.R - a.R) * t,
                        a.G + (b.G - a.G) * t,
                        a.B + (b.B - a.B) * t,
                        a.A + (b.A - a.A) * t
                );
    }
}
