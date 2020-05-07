package jav;

import static jav.utils.TrigonometricUtils.*;
import static jav.utils.TrigonometricUtils.dTan;

public class SalatTimeCalculationMethod {
    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    public static double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixAngle(357.529 + 0.98560028 * D);
        double q = fixAngle(280.459 + 0.98564736 * D);
        double L = fixAngle(q + (1.915 * dSin(g)) + (0.020 * dSin(2 * g)));

        // double R = 1.00014 - 0.01671 * [self kt.dCos:g] - 0.00014 * [self kt.dCos:
        // (2*g)];
        double e = 23.439 - (0.00000036 * D);
        double d = dArcSin(dSin(e) * dSin(L));
        double RA = (dArcTan2((dCos(e) * dSin(L)), (dCos(L)))) / 15.0;
        RA = fixHour(RA);
        double EqT = q / 15.0 - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    // compute equation of time
    public static double equationOfTime(double jd) {
        double eq = sunPosition(jd)[1];
        return eq;
    }

    // compute declination angle of sun
    public static double sunDeclination(double jd) {
        double d = sunPosition(jd)[0];
        return d;
    }

    // compute mid-day (Dhuhr, Zawal) time
    public static double computeMidDay(double t,double jDate) {
        double T = equationOfTime(jDate + t);
        double Z = fixHour(12 - T);
        return Z;
    }

    // compute time for a given angle G
    public static double computeTime(double G, double t,double jDate,double lat) {

        double D = sunDeclination(jDate + t);
        double Z = computeMidDay(t,jDate);
        double Beg = -dSin(G) - dSin(D) * dSin(lat);
        double Mid = dCos(D) * dCos(lat);
        double V = dArcCos(Beg / Mid) / 15.0;

        return Z + (G > 90 ? -V : V);
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    public static double computeAsr(double step, double t,double jDate,double lat) {
        double D = sunDeclination(jDate + t);
        double G = -dArcCot(step + dTan(Math.abs(lat - D)));
        return computeTime(G, t,jDate,lat);
    }
}
