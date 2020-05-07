package kt

import java.util.HashMap

enum class CalculationMethod {
    JAFARI, // Ithna Ashari
    KARACHI, // University of Islamic Sciences, Karachi
    ISNA, // Islamic Society of North America (ISNA)
    MWL, // Muslim World League (MWL)
    MAKKAH, // Umm al-Qura, Makkah
    EGYPT, // Egyptian General Authority of Survey
    TEHRAN, // Institute of Geophysics, University of Tehran
    CUSTOM // Custom Setting
}

class CalculationMethodParams {
    private var methodParams: HashMap<CalculationMethod, DoubleArray> = HashMap()

    constructor() {
        /*
        *
        * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
        * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
        * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
        * value (in angle or minutes)
        */

        // Jafari
        val Jvalues = doubleArrayOf(16.0, 0.0, 4.0, 0.0, 14.0)
        methodParams.put(CalculationMethod.JAFARI, Jvalues)

        // Karachi
        val Kvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 18.0)
        methodParams.put(CalculationMethod.KARACHI, Kvalues)

        // ISNA
        val Ivalues = doubleArrayOf(15.0, 1.0, 0.0, 0.0, 15.0)
        methodParams.put(CalculationMethod.ISNA, Ivalues)

        // MWL
        val MWvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams.put(CalculationMethod.MWL, MWvalues)

        // Makkah
        val MKvalues = doubleArrayOf(18.5, 1.0, 0.0, 1.0, 90.0)
        methodParams.put(CalculationMethod.MAKKAH, MKvalues)

        // Egypt
        val Evalues = doubleArrayOf(19.5, 1.0, 0.0, 0.0, 17.5)
        methodParams.put(CalculationMethod.EGYPT, Evalues)

        // Tehran
        val Tvalues = doubleArrayOf(17.7, 0.0, 4.5, 0.0, 14.0)
        methodParams.put(CalculationMethod.TEHRAN, Tvalues)

        // Custom
        val Cvalues = doubleArrayOf(18.0, 1.0, 0.0, 0.0, 17.0)
        methodParams.put(CalculationMethod.CUSTOM, Cvalues)
    }

    fun get(calculationMethod: CalculationMethod): DoubleArray = methodParams[calculationMethod]!!
    fun put(custom: CalculationMethod, params: DoubleArray) {
        methodParams.put(custom, params)
    }

}