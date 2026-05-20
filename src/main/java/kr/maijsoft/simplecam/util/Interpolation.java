package kr.maijsoft.simplecam.util;

public final class Interpolation {

    private Interpolation() { }

    /**
     * Smoothstep easing on a normalized parameter t in [0, 1].
     * Used to soften the in/out of each segment.
     */
    public static double smoothstep(double t) {
        if (t <= 0.0) return 0.0;
        if (t >= 1.0) return 1.0;
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Catmull-Rom spline at parameter t in [0, 1] between p1 and p2,
     * using p0 and p3 as tangent controls.
     */
    public static double catmullRom(double p0, double p1, double p2, double p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * (
                (2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
        );
    }

    /**
     * Shortest-arc lerp for yaw/pitch in degrees.
     */
    public static float lerpAngle(float a, float b, double t) {
        float diff = ((b - a) % 360f + 540f) % 360f - 180f;
        return a + (float) (diff * t);
    }
}
