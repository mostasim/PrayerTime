package jav;
/*

PrayTime.java: Prayer Times Calculator (ver 1.0)
Copyright (C) 2007-2010 PrayTimes.org

Java Code By: Hussain Ali Khan
Original JS Code By: Hamid Zarrabi-Zadeh

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or
	without modification, in any website or application
	provided that credit is given to the original work
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will
be useful, but WITHOUT ANY WARRANTY.

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/


import jav.utils.TimeZoneUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.spi.TimeZoneNameProvider;

import static jav.SalatTimeCalculationMethod.computeAsr;
import static jav.SalatTimeCalculationMethod.computeMidDay;
import static jav.SalatTimeCalculationMethod.computeTime;
import static jav.utils.JulianDateUtils.julianDate;
import static jav.utils.TimeFormatUtils.floatToTime12;
import static jav.utils.TimeFormatUtils.floatToTime24;
import static jav.utils.TrigonometricUtils.*;

public class PrayTime {

    // ---------------------- Global Variables --------------------
    private CalculationMethod calcMethod; // calculation method
    private JuristicMethod asrJuristic; // Juristic method for Asr
    private int dhuhrMinutes; // minutes after mid-day for Dhuhr
    private AdjustingMethod adjustHighLats; // adjusting method for higher latitudes
    private TimeFormat timeFormat; // time format
    private double lat; // latitude
    private double lng; // longitude
    private double timeZone; // time-zone
    private double JDate; // Julian date
    // ------------------------------------------------------------
    // Calculation Methods
    private CalculationMethod Jafari; // Ithna Ashari
    private CalculationMethod Karachi; // University of Islamic Sciences, Karachi
    private CalculationMethod ISNA; // Islamic Society of North America (ISNA)
    private CalculationMethod MWL; // Muslim World League (MWL)
    private CalculationMethod Makkah; // Umm al-Qura, Makkah
    private CalculationMethod Egypt; // Egyptian General Authority of Survey
    private CalculationMethod Tehran; // Institute of Geophysics, University of Tehran
    private CalculationMethod Custom; // Custom Setting
    // Juristic Methods
    private JuristicMethod Shafii; // Shafii (standard)
    private JuristicMethod Hanafi; // Hanafi
    // Adjusting Methods for Higher Latitudes
    private AdjustingMethod None; // No adjustment
    private AdjustingMethod MidNight; // middle of night
    private AdjustingMethod OneSeventh; // 1/7th of night
    private AdjustingMethod AngleBased; // angle/60th of night
    // Time Formats
    private TimeFormat Time24; // 24-hour format
    private TimeFormat Time12; // 12-hour format
    private TimeFormat Time12NS; // 12-hour format with no suffix
    private TimeFormat Floating; // floating point number
    // Time Names
    private ArrayList<String> timeNames;
    private String InvalidTime; // The string used for invalid times
    // --------------------- Technical Settings --------------------
    private int numIterations; // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------
    private HashMap<CalculationMethod, double[]> methodParams;

    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     *
     * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
     * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
     * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
     * (in angle or minutes)
     */
    private double[] prayerTimesCurrent;
    private int[] offsets;

    public PrayTime() {
        // Initialize vars

        this.setCalcMethod(CalculationMethod.KARACHI);
        this.setAsrJuristic(JuristicMethod.HANAFI);
        this.setDhuhrMinutes(0);
        this.setAdjustHighLats(AdjustingMethod.AngleBased);
        this.setTimeFormat(TimeFormat.Time12);


        // Time Names
        timeNames = new ArrayList<String>();
        timeNames.add("Fajr");
        timeNames.add("Sunrise");
        timeNames.add("Dhuhr");
        timeNames.add("Asr");
        timeNames.add("Sunset");
        timeNames.add("Maghrib");
        timeNames.add("Isha");

        InvalidTime = "-----"; // The string used for invalid times

        // --------------------- Technical Settings --------------------

        this.setNumIterations(1); // number of iterations needed to compute
        // times

        // ------------------- Calc Method Parameters --------------------

        // Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
        offsets = new int[7];
        offsets[0] = 0;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        offsets[5] = 0;
        offsets[6] = 0;

        /*
         *
         * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
         * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
         * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
         * value (in angle or minutes)
         */
        methodParams = new HashMap<CalculationMethod, double[]>();

        // Jafari
        double[] Jvalues = {16, 0, 4, 0, 14};
        methodParams.put(CalculationMethod.JAFARI, Jvalues);

        // Karachi
        double[] Kvalues = {18, 1, 0, 0, 18};
        methodParams.put(CalculationMethod.KARACHI, Kvalues);

        // ISNA
        double[] Ivalues = {15, 1, 0, 0, 15};
        methodParams.put(CalculationMethod.ISNA, Ivalues);

        // MWL
        double[] MWvalues = {18, 1, 0, 0, 17};
        methodParams.put(CalculationMethod.MWL, MWvalues);

        // Makkah
        double[] MKvalues = {18.5, 1, 0, 1, 90};
        methodParams.put(CalculationMethod.MAKKAH, MKvalues);

        // Egypt
        double[] Evalues = {19.5, 1, 0, 0, 17.5};
        methodParams.put(CalculationMethod.EGYPT, Evalues);

        // Tehran
        double[] Tvalues = {17.7, 0, 4.5, 0, 14};
        methodParams.put(CalculationMethod.TEHRAN, Tvalues);

        // Custom
        double[] Cvalues = {18, 1, 0, 0, 17};
        methodParams.put(CalculationMethod.CUSTOM, Cvalues);

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        double latitude = 40.730610;
        double longitude = -73.935242;
        double timezone = -4;
        // Test Prayer times here
        PrayTime prayers = new PrayTime();

        prayers.setTimeFormat(TimeFormat.Time12);
        prayers.setCalcMethod(CalculationMethod.ISNA);
        prayers.setAsrJuristic(JuristicMethod.SHAFI);
        prayers.setAdjustHighLats(AdjustingMethod.AngleBased);
//        prayers.setFajrAngle(15.0);
//        prayers.setIshaAngle(15.0);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
        prayers.tune(offsets);

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        ArrayList<String> prayerTimes = prayers.getPrayerTimes(cal,
                latitude, longitude, timezone);
        ArrayList<String> prayerNames = prayers.getTimeNames();

        for (int i = 0; i < prayerTimes.size(); i++) {
            System.out.println(prayerNames.get(i) + " - " + prayerTimes.get(i));
        }

        TimeZone timeZone = TimeZone.getDefault();
        ZoneId zoneId = timeZone.toZoneId();
        TimeZoneUtils.getBaseTimeZone();
        double hoursDiff = (TimeZone.getTimeZone(zoneId).getRawOffset() / 1000.0) / 3600;
        System.out.println(Arrays.toString(TimeZone.getAvailableIDs()));

    }


    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixHour(time2 - time1);
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private ArrayList<String> getDatePrayerTimes(int year, int month, int day,
                                                 double latitude, double longitude, double tZone) {
        this.setLat(latitude);
        this.setLng(longitude);
        this.setTimeZone(tZone);
        this.setJDate(julianDate(year, month, day));
        double lonDiff = longitude / (15.0 * 24.0);
        this.setJDate(this.getJDate() - lonDiff);
        return computeDayTimes();
    }

    // return prayer times for a given date
    private ArrayList<String> getPrayerTimes(Calendar date, double latitude,
                                             double longitude, double tZone) {

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);

        return getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone);
    }

    // set custom values for calculation parameters
    private void setCustomParams(double[] params) {

        for (int i = 0; i < 5; i++) {
            if (params[i] == -1) {
                params[i] = methodParams.get(this.getCalcMethod())[i];
                methodParams.put(CalculationMethod.CUSTOM, params);
            } else {
                methodParams.get(CalculationMethod.CUSTOM)[i] = params[i];
            }
        }
        this.setCalcMethod(CalculationMethod.CUSTOM);
    }

    // set the angle for calculating Fajr
    public void setFajrAngle(double angle) {
        double[] params = {angle, -1, -1, -1, -1};
        setCustomParams(params);
    }

    // set the angle for calculating Maghrib
    public void setMaghribAngle(double angle) {
        double[] params = {-1, 0, angle, -1, -1};
        setCustomParams(params);

    }

    // set the angle for calculating Isha
    public void setIshaAngle(double angle) {
        double[] params = {-1, -1, -1, 0, angle};
        setCustomParams(params);

    }

    // set the minutes after Sunset for calculating Maghrib
    public void setMaghribMinutes(double minutes) {
        double[] params = {-1, 1, minutes, -1, -1};
        setCustomParams(params);

    }

    // set the minutes after Maghrib for calculating Isha
    public void setIshaMinutes(double minutes) {
        double[] params = {-1, -1, -1, 1, minutes};
        setCustomParams(params);

    }


    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private double[] computeTimes(double[] times) {

        double[] dayPortion = dayPortion(times);

        double fajr = computeTime(180 - methodParams.get(this.getCalcMethod())[0], dayPortion[0], getJDate(), getLat());

        double sunrise = computeTime(180 - 0.833, dayPortion[1], getJDate(), getLat());

        double dhuhr = computeMidDay(dayPortion[2], getJDate());
        double asr = computeAsr(1 + this.getAsrJuristic().ordinal(), dayPortion[3], getJDate(), getLat());
        double sunset = computeTime(0.833, dayPortion[4], getJDate(), getLat());

        double maghrib = computeTime(
                methodParams.get(this.getCalcMethod())[2], dayPortion[5], getJDate(), getLat());
        double isha = computeTime(
                methodParams.get(this.getCalcMethod())[4], dayPortion[6], getJDate(), getLat());

        double[] computeTimes = {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha};

        return computeTimes;

    }

    // compute prayer times at given julian date
    private ArrayList<String> computeDayTimes() {
        double[] times = {5, 6, 12, 13, 18, 18, 18}; // default times

        for (int i = 1; i <= this.getNumIterations(); i++) {
            times = computeTimes(times);
        }

        times = adjustTimes(times);
        times = tuneTimes(times);

        return adjustTimesFormat(times);
    }

    // adjust times in a prayer time array
    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += this.getTimeZone() - this.getLng() / 15;
        }

        times[2] += this.getDhuhrMinutes() / 60; // Dhuhr
        if (methodParams.get(this.getCalcMethod())[1] == 1) // Maghrib
        {
            times[5] = times[4] + methodParams.get(this.getCalcMethod())[2] / 60;
        }
        if (methodParams.get(this.getCalcMethod())[3] == 1) // Isha
        {
            times[6] = times[5] + methodParams.get(this.getCalcMethod())[4] / 60;
        }

        if (this.getAdjustHighLats() != AdjustingMethod.None) {
            times = adjustHighLatTimes(times);
        }

        return times;
    }

    // convert times array to given time format
    private ArrayList<String> adjustTimesFormat(double[] times) {

        ArrayList<String> result = new ArrayList<String>();

        if (this.getTimeFormat() == TimeFormat.Floating) {
            for (double time : times) {
                result.add(String.valueOf(time));
            }
            return result;
        }

        for (int i = 0; i < 7; i++) {
            if (this.getTimeFormat() == TimeFormat.Time12) {
                result.add(floatToTime12(times[i], false));
            } else if (this.getTimeFormat() == TimeFormat.Time12NS) {
                result.add(floatToTime12(times[i], true));
            } else {
                result.add(floatToTime24(times[i]));
            }
        }
        return result;
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = timeDiff(times[4], times[1]); // sunset to sunrise

        // Adjust Fajr
        double FajrDiff = nightPortion(methodParams.get(this.getCalcMethod())[0]) * nightTime;

        if (Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff;
        }

        // Adjust Isha
        double IshaAngle = (methodParams.get(this.getCalcMethod())[3] == 0) ? methodParams.get(this.getCalcMethod())[4] : 18;
        double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
        if (Double.isNaN(times[6]) || this.timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff;
        }

        // Adjust Maghrib
        double MaghribAngle = (methodParams.get(this.getCalcMethod())[1] == 0) ? methodParams.get(this.getCalcMethod())[2] : 4;
        double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
        if (Double.isNaN(times[5]) || this.timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff;
        }

        return times;
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle) {
        double calc = 0;

        if (adjustHighLats == AdjustingMethod.AngleBased)
            calc = (angle) / 60.0;
        else if (adjustHighLats == AdjustingMethod.MidNight)
            calc = 0.5;
        else if (adjustHighLats == AdjustingMethod.OneSeventh)
            calc = 0.14286;

        return calc;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++) {
            times[i] /= 24;
        }
        return times;
    }

    // Tune timings for adjustments
    // Set time offsets
    public void tune(int[] offsetTimes) {

        for (int i = 0; i < offsetTimes.length; i++) { // offsetTimes length
            // should be 7 in order
            // of Fajr, Sunrise,
            // Dhuhr, Asr, Sunset,
            // Maghrib, Isha
            this.offsets[i] = offsetTimes[i];
        }
    }

    private double[] tuneTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + this.offsets[i] / 60.0;
        }

        return times;
    }

    public CalculationMethod getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(CalculationMethod calcMethod) {
        this.calcMethod = calcMethod;
    }

    public JuristicMethod getAsrJuristic() {
        return asrJuristic;
    }

    public void setAsrJuristic(JuristicMethod asrJuristic) {
        this.asrJuristic = asrJuristic;
    }

    public int getDhuhrMinutes() {
        return dhuhrMinutes;
    }

    public void setDhuhrMinutes(int dhuhrMinutes) {
        this.dhuhrMinutes = dhuhrMinutes;
    }

    public AdjustingMethod getAdjustHighLats() {
        return adjustHighLats;
    }

    public void setAdjustHighLats(AdjustingMethod adjustHighLats) {
        this.adjustHighLats = adjustHighLats;
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    public double getJDate() {
        return JDate;
    }

    public void setJDate(double jDate) {
        JDate = jDate;
    }

    private CalculationMethod getJafari() {
        return Jafari;
    }

    private void setJafari(CalculationMethod jafari) {
        Jafari = jafari;
    }

    private CalculationMethod getKarachi() {
        return Karachi;
    }

    private void setKarachi(CalculationMethod karachi) {
        Karachi = karachi;
    }

    private CalculationMethod getISNA() {
        return ISNA;
    }

    private void setISNA(CalculationMethod iSNA) {
        ISNA = iSNA;
    }

    private CalculationMethod getMWL() {
        return MWL;
    }

    private void setMWL(CalculationMethod mWL) {
        MWL = mWL;
    }

    private CalculationMethod getMakkah() {
        return Makkah;
    }

    private void setMakkah(CalculationMethod makkah) {
        Makkah = makkah;
    }

    private CalculationMethod getEgypt() {
        return Egypt;
    }

    private void setEgypt(CalculationMethod egypt) {
        Egypt = egypt;
    }

    private CalculationMethod getCustom() {
        return Custom;
    }

    private void setCustom(CalculationMethod custom) {
        Custom = custom;
    }

    private CalculationMethod getTehran() {
        return Tehran;
    }

    private void setTehran(CalculationMethod tehran) {
        Tehran = tehran;
    }

    private JuristicMethod getShafii() {
        return Shafii;
    }

    private void setShafii(JuristicMethod shafii) {
        Shafii = shafii;
    }

    private JuristicMethod getHanafi() {
        return Hanafi;
    }

    private void setHanafi(JuristicMethod hanafi) {
        Hanafi = hanafi;
    }

    private AdjustingMethod getNone() {
        return None;
    }

    private void setNone(AdjustingMethod none) {
        None = none;
    }

    private AdjustingMethod getMidNight() {
        return MidNight;
    }

    private void setMidNight(AdjustingMethod midNight) {
        MidNight = midNight;
    }

    private AdjustingMethod getOneSeventh() {
        return OneSeventh;
    }

    private void setOneSeventh(AdjustingMethod oneSeventh) {
        OneSeventh = oneSeventh;
    }

    private AdjustingMethod getAngleBased() {
        return AngleBased;
    }

    private void setAngleBased(AdjustingMethod angleBased) {
        AngleBased = angleBased;
    }

    private TimeFormat getTime24() {
        return Time24;
    }

    private void setTime24(TimeFormat time24) {
        Time24 = time24;
    }

    private TimeFormat getTime12() {
        return Time12;
    }

    private void setTime12(TimeFormat time12) {
        Time12 = time12;
    }

    private TimeFormat getTime12NS() {
        return Time12NS;
    }

    private void setTime12NS(TimeFormat time12ns) {
        Time12NS = time12ns;
    }

    private TimeFormat getFloating() {
        return Floating;
    }

    private void setFloating(TimeFormat floating) {
        Floating = floating;
    }

    private int getNumIterations() {
        return numIterations;
    }

    private void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public ArrayList<String> getTimeNames() {
        return timeNames;
    }
}