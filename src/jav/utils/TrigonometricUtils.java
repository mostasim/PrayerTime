package jav.utils;

public class TrigonometricUtils {
    // ---------------------- TrigonometricUtils Functions -----------------------
    // range reduce angle in degrees.
    public static double fixAngle(double a) {

        a = a - (360 * (Math.floor(a / 360.0)));

        a = a < 0 ? (a + 360) : a;

        return a;
    }

    // range reduce hours to 0..23
    public static double fixHour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        a = a < 0 ? (a + 24) : a;
        return a;
    }

    // radian to degree
    public static double radiansToDegrees(double alpha) {
        return ((alpha * 180.0) / Math.PI);
    }

    // deree to radian
    public static double DegreesToRadians(double alpha) {
        return ((alpha * Math.PI) / 180.0);
    }

    // degree sin
    public static double dSin(double d) {
        return (Math.sin(DegreesToRadians(d)));
    }

    // degree cos
    public static double dCos(double d) {
        return (Math.cos(DegreesToRadians(d)));
    }

    // degree tan
    public static double dTan(double d) {
        return (Math.tan(DegreesToRadians(d)));
    }

    // degree arcsin
    public static double dArcSin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    // degree arccos
    public static double dArcCos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    // degree arctan
    public static double dArcTan(double x) {
        double val = Math.atan(x);
        return radiansToDegrees(val);
    }

    // degree arctan2
    public static double dArcTan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    // degree arccot
    public static double dArcCot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }

}
