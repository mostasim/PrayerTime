package kt

import java.util.*

class PrayerTime {


    // ---------------------- Global Variables --------------------
    var calcMethod: CalculationMethod // calculation method
        set(value) {
            field = value
        }
    var asrJuristic: JuristicMethod// Juristic method for Asr
        set(value) {
            field = value
        }
    var dhuhrMinutes: Int = 0 // minutes after mid-day for Dhuhr
    var adjustHighLats: AdjustingMethod// adjusting method for higher latitudes
        set(value) {
            field = value
        }
    var timeFormat: TimeFormat // time format
        set(value) {
            field = value
        }
    var lat: Double = 0.toDouble() // latitude
        set(value) {
            field = value
        }
    var lng: Double = 0.toDouble() // longitude
        set(value) {
            field = value
        }
    var timeZone: Double = 0.toDouble() // time-zone
        set(value) {
            field = value
        }
    var JDate: Double = 0.toDouble() // Julian date
        set(value) {
            field = value
        }

    // Time Names
    lateinit var timeNames: ArrayList<String>
    lateinit var InvalidTime: String // The string used for invalid times
    // --------------------- Technical Settings --------------------
    var numIterations: Int = 1 // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------

    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     *
     * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
     * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
     * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
     * (in angle or minutes)
     */
    val methodParams = CalculationMethodParams()
    var offsets: IntArray

    constructor() {
        calcMethod = CalculationMethod.KARACHI
        asrJuristic = JuristicMethod.SHAFI
        dhuhrMinutes = 0
        adjustHighLats = AdjustingMethod.AngleBased
        timeFormat = TimeFormat.Time12

        timeNames = ArrayList()
        timeNames.add("Fajr")
        timeNames.add("Sunrise")
        timeNames.add("Dhuhr")
        timeNames.add("Asr")
        timeNames.add("Sunset")
        timeNames.add("Maghrib")
        timeNames.add("Isha")

        InvalidTime = "-----" // The string used for invalid times

        // --------------------- Technical Settings --------------------

        numIterations = 1// number of iterations needed to compute
        // times

        // ------------------- Calc Method Parameters --------------------

        // Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
        offsets = IntArray(7)
        offsets[0] = 0
        offsets[1] = 0
        offsets[2] = 0
        offsets[3] = 0
        offsets[4] = 0
        offsets[5] = 0
        offsets[6] = 0


    }


    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private fun getTimeZone1(): Double {
        val timez = TimeZone.getDefault()
        return timez.rawOffset / 1000.0 / 3600
    }

    // compute base time-zone of the system
    private fun getBaseTimeZone(): Double {
        val timez = TimeZone.getDefault()
        return timez.rawOffset / 1000.0 / 3600

    }

    // detect daylight saving in a given date
    private fun detectDaylightSaving(): Double {
        val timez = TimeZone.getDefault()
        return timez.dstSavings.toDouble()
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var year = year
        var month = month

        if (month <= 2) {
            year -= 1
            month += 12
        }
        val A = Math.floor(year / 100.0)

        val B = 2 - A + Math.floor(A / 4.0)

        return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day.toDouble() + B - 1524.5
    }

    // convert a calendar date to julian date (second method)
    private fun calcJD(year: Int, month: Int, day: Int): Double {
        val J1970 = 2440588.0
        val date = GregorianCalendar(year, month - 1, day)

        val ms = date.time.time // # of milliseconds since midnight Jan 1,
        // 1970
        val days = Math.floor(ms / (1000.0 * 60.0 * 60.0 * 24.0))
        return days + J1970 - 0.5

    }


    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private fun sunPosition(jd: Double): DoubleArray {

        val D = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * D)
        val q = fixAngle(280.459 + 0.98564736 * D)
        val L = fixAngle(q + 1.915 * dSin(g) + 0.020 * dSin(2 * g))

        // double R = 1.00014 - 0.01671 * [self kt.dCos:g] - 0.00014 * [self kt.dCos:
        // (2*g)];
        val e = 23.439 - 0.00000036 * D
        val d = darcsin(dSin(e) * dSin(L))
        var RA = darctan2(dCos(e) * dSin(L), dCos(L)) / 15.0
        RA = fixHour(RA)
        val EqT = q / 15.0 - RA
        val sPosition = DoubleArray(2)
        sPosition[0] = d
        sPosition[1] = EqT

        return sPosition
    }

    // compute equation of time
    private fun equationOfTime(jd: Double): Double {
        return sunPosition(jd)[1]
    }

    // compute declination angle of sun
    private fun sunDeclination(jd: Double): Double {
        return sunPosition(jd)[0]
    }

    // compute mid-day (Dhuhr, Zawal) time
    private fun computeMidDay(t: Double): Double {
        val T = equationOfTime(+t)
        return fixHour(12 - T)
    }

    // compute time for a given angle G
    private fun computeTime(G: Double, t: Double): Double {

        val D = sunDeclination(JDate + t)
        val Z = computeMidDay(t)
        val Beg = -dSin(G) - dSin(D) * dSin(lat)
        val Mid = dCos(D) * dCos(lat)
        val V = darccos(Beg / Mid) / 15.0

        return Z + if (G > 90) -V else V
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private fun computeAsr(step: Double, t: Double): Double {
        val D = sunDeclination(JDate + t)
        val G = -darccot(step + dtan(Math.abs(lat - D)))
        return computeTime(G, t)
    }


    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private fun timeDiff(time1: Double, time2: Double): Double {
        return fixHour(time2 - time1)
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private fun getDatePrayerTimes(year: Int, month: Int, day: Int,
                                   latitude: Double, longitude: Double, tZone: Double): ArrayList<String> {
        lat = latitude
        lng = longitude
        timeZone = tZone
        JDate = (julianDate(year, month, day))
        val lonDiff = longitude / (15.0 * 24.0)
        JDate = (JDate - lonDiff)
        return computeDayTimes()
    }

    // return prayer times for a given date
    fun getPrayerTimes(date: Calendar, latitude: Double,
                       longitude: Double, tZone: Double): ArrayList<String> {

        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)
        val day = date.get(Calendar.DATE)

        return getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone)
    }

    // set custom values for calculation parameters
    private fun setCustomParams(params: DoubleArray) {

        for (i in 0..4) {
            if (params[i] == -1.0) {
                params[i] = methodParams.get(calcMethod)[i]
                methodParams.put(CalculationMethod.CUSTOM, params)
            } else {
                methodParams.get(CalculationMethod.CUSTOM)[i] = params[i]
            }
        }
        calcMethod = CalculationMethod.CUSTOM
    }

    // set the angle for calculating Fajr
    fun setFajrAngle(angle: Double) {
        val params = doubleArrayOf(angle, -1.0, -1.0, -1.0, -1.0)
        setCustomParams(params)
    }

    // set the angle for calculating Maghrib
    fun setMaghribAngle(angle: Double) {
        val params = doubleArrayOf(-1.0, 0.0, angle, -1.0, -1.0)
        setCustomParams(params)

    }

    // set the angle for calculating Isha
    fun setIshaAngle(angle: Double) {
        val params = doubleArrayOf(-1.0, -1.0, -1.0, 0.0, angle)
        setCustomParams(params)

    }

    // set the minutes after Sunset for calculating Maghrib
    fun setMaghribMinutes(minutes: Double) {
        val params = doubleArrayOf(-1.0, 1.0, minutes, -1.0, -1.0)
        setCustomParams(params)

    }

    // set the minutes after Maghrib for calculating Isha
    fun setIshaMinutes(minutes: Double) {
        val params = doubleArrayOf(-1.0, -1.0, -1.0, 1.0, minutes)
        setCustomParams(params)

    }

    // convert double hours to 24h format
    fun floatToTime24(time: Double): String {
        var time = time

        val result: String

        if (java.lang.Double.isNaN(time)) {
            return InvalidTime
        }

        time = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
        val hours = Math.floor(time).toInt()
        val minutes = Math.floor((time - hours) * 60.0)

        result = if (hours in 0..9 && minutes >= 0 && minutes <= 9) {
            "0" + hours + ":0" + Math.round(minutes)
        } else if (hours in 0..9) {
            "0" + hours + ":" + Math.round(minutes)
        } else if (minutes in 0.0..9.0) {
            hours.toString() + ":0" + Math.round(minutes)
        } else {
            hours.toString() + ":" + Math.round(minutes)
        }
        return result
    }

    // convert double hours to 12h format
    fun floatToTime12(time: Double, noSuffix: Boolean): String {
        var time = time

        if (java.lang.Double.isNaN(time)) {
            return InvalidTime
        }

        time = fixHour(time + 0.5 / 60.0) // add 0.5 minutes to round
        var hours = Math.floor(time).toInt()
        val minutes = Math.floor((time - hours) * 60.0)
        val suffix: String
        val result: String
        suffix = if (hours >= 12) {
            "pm"
        } else {
            "am"
        }
        hours = (hours + 12 - 1) % 12 + 1
        /*hours = (hours + 12) - 1;
        int hrs = (int) hours % 12;
        hrs += 1;*/
        if (noSuffix == false) {
            result = if (hours in 0..9 && minutes >= 0 && minutes <= 9) {
                "0" + hours + ":0" + Math.round(minutes) + " " + suffix
            } else if (hours in 0..9) {
                "0" + hours + ":" + Math.round(minutes) + " " + suffix
            } else if (minutes in 0.0..9.0) {
                hours.toString() + ":0" + Math.round(minutes) + " " + suffix
            } else {
                hours.toString() + ":" + Math.round(minutes) + " " + suffix
            }

        } else {
            result = if (hours in 0..9 && minutes >= 0 && minutes <= 9) {
                "0" + hours + ":0" + Math.round(minutes)
            } else if (hours in 0..9) {
                "0" + hours + ":" + Math.round(minutes)
            } else if (minutes in 0.0..9.0) {
                hours.toString() + ":0" + Math.round(minutes)
            } else {
                hours.toString() + ":" + Math.round(minutes)
            }
        }
        return result

    }

    // convert double hours to 12h format with no suffix
    fun floatToTime12NS(time: Double): String {
        return floatToTime12(time, true)
    }


    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private fun computeTimes(times: DoubleArray): DoubleArray {

        val t = dayPortion(times)

        val Fajr = this.computeTime(180.0 - methodParams.get(calcMethod)[0], t[0])

        val Sunrise = this.computeTime(180.0 - 0.833, t[1])

        val Dhuhr = this.computeMidDay(t[2])
        val Asr = this.computeAsr((1 + asrJuristic.ordinal).toDouble(), t[3])
        val Sunset = this.computeTime(0.833, t[4])

        val Maghrib = this.computeTime(methodParams.get(calcMethod)[2], t[5])
        val Isha = this.computeTime(methodParams.get(calcMethod)[4], t[6])

        return doubleArrayOf(Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha)

    }

    // compute prayer times at given julian date
    private fun computeDayTimes(): ArrayList<String> {
        var times = doubleArrayOf(5.0, 6.0, 12.0, 13.0, 18.0, 18.0, 18.0) // default times

        for (i in 1..numIterations) {
            times = computeTimes(times)
        }

        times = adjustTimes(times)
        times = tuneTimes(times)

        return adjustTimesFormat(times)
    }

    // adjust times in a prayer time array
    private fun adjustTimes(times: DoubleArray): DoubleArray {
        var times = times
        for (i in times.indices) {
            times[i] += timeZone - lng / 15
        }

        times[2] += (dhuhrMinutes / 60).toDouble() // Dhuhr
        if (methodParams.get(calcMethod)[1] == 1.0)// Maghrib
        {
            times[5] = times[4] + methodParams.get(calcMethod)[2] / 60.0
        }
        if (methodParams.get(calcMethod)[3] == 1.0)// Isha
        {
            times[6] = times[5] + methodParams.get(calcMethod)[4] / 60.0
        }

        if (adjustHighLats != AdjustingMethod.None) {
            times = adjustHighLatTimes(times)
        }

        return times
    }

    // convert times array to given time format
    private fun adjustTimesFormat(times: DoubleArray): ArrayList<String> {

        val result = ArrayList<String>()

        if (timeFormat == TimeFormat.Floating) {
            times.mapTo(result) { it.toString() }
            return result
        }

        for (i in 0..6) {
            when (timeFormat) {
                TimeFormat.Time12 -> result.add(floatToTime12(times[i], false))
                TimeFormat.Time12NS -> result.add(floatToTime12(times[i], true))
                else -> result.add(floatToTime24(times[i]))
            }
        }
        return result
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private fun adjustHighLatTimes(times: DoubleArray): DoubleArray {
        val nightTime = timeDiff(times[4], times[1]) // sunset to sunrise

        // Adjust Fajr
        val FajrDiff = nightPortion(methodParams.get(calcMethod)[0]) * nightTime

        if (java.lang.Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff
        }

        // Adjust Isha
        val IshaAngle: Double = if (methodParams.get(calcMethod)[3] == 0.0) methodParams.get(calcMethod)[4] else 18.0
        val IshaDiff = this.nightPortion(IshaAngle) * nightTime
        if (java.lang.Double.isNaN(times[6]) || this.timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff
        }

        // Adjust Maghrib
        val MaghribAngle: Double = if (methodParams.get(calcMethod)[1] == 0.0) methodParams.get(calcMethod)[2] else 4.0
        val MaghribDiff = nightPortion(MaghribAngle) * nightTime
        if (java.lang.Double.isNaN(times[5]) || this.timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff
        }

        return times
    }

    // the night portion used for adjusting times in higher latitudes
    private fun nightPortion(angle: Double): Double {
        var calc = 0.0

        if (adjustHighLats == AdjustingMethod.AngleBased)
            calc = angle / 60.0
        else if (adjustHighLats == AdjustingMethod.MidNight)
            calc = 0.5
        else if (adjustHighLats == AdjustingMethod.OneSeventh)
            calc = 0.14286

        return calc
    }

    // convert hours to day portions
    private fun dayPortion(times: DoubleArray): DoubleArray {
        for (i in 0..6) {
            times[i] /= 24.0
        }
        return times
    }

    // Tune timings for adjustments
    // Set time offsets
    fun tune(offsetTimes: IntArray) {

        for (i in offsetTimes.indices) { // offsetTimes length
            // should be 7 in order
            // of Fajr, Sunrise,
            // Dhuhr, Asr, Sunset,
            // Maghrib, Isha
            this.offsets[i] = offsetTimes[i]
        }
    }

    private fun tuneTimes(times: DoubleArray): DoubleArray {
        for (i in times.indices) {
            times[i] = times[i] + this.offsets[i] / 60.0
        }

        return times
    }


}

fun main(args: Array<String>) {
    print("Hello ${CalculationMethod.JAFARI.ordinal}")
    val latitude = 23.810499
    val longitude = 90.411651
    val timezone = 6.0

    val prayerTime = PrayerTime()
    prayerTime.calcMethod = CalculationMethod.KARACHI
    prayerTime.timeFormat = TimeFormat.Time12
    prayerTime.asrJuristic = JuristicMethod.SHAFI
    prayerTime.adjustHighLats = AdjustingMethod.AngleBased
    val offsets = intArrayOf(0, 0, 0, 0, 0, 0, 0) // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}
    prayerTime.tune(offsets)

    val now = Date()
    val cal = Calendar.getInstance()
    cal.time = now

    println(cal.time)
    val prayerTimes = prayerTime.getPrayerTimes(cal, latitude, longitude, timezone)
    val prayerNames = prayerTime.timeNames

    for (i in prayerTimes.indices) {
        println(prayerNames.get(i) + " - " + prayerTimes.get(i))
    }


}