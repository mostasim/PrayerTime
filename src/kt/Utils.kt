package kt

// ---------------------- TrigonometricUtils Functions -----------------------
// range reduce angle in degrees.
 fun fixAngle(a: Double): Double {
    var a = a

    a -= 360 * Math.floor(a / 360.0)

    a = if (a < 0) a + 360 else a

    return a
}

// range reduce hours to 0..23
 fun fixHour(a: Double): Double {
    var a = a
    a -= 24.0 * Math.floor(a / 24.0)
    a = if (a < 0) a + 24 else a
    return a
}

// radian to degree
 fun radiansToDegrees(alpha: Double): Double {
    return alpha * 180.0 / Math.PI
}

// deree to radian
 fun DegreesToRadians(alpha: Double): Double {
    return alpha * Math.PI / 180.0
}

// degree sin
 fun dSin(d: Double): Double {
    return Math.sin(DegreesToRadians(d))
}

// degree cos
 fun dCos(d: Double): Double {
    return Math.cos(DegreesToRadians(d))
}

// degree tan
 fun dtan(d: Double): Double {
    return Math.tan(DegreesToRadians(d))
}

// degree arcsin
 fun darcsin(x: Double): Double {
    val `val` = Math.asin(x)
    return radiansToDegrees(`val`)
}

// degree arccos
 fun darccos(x: Double): Double {
    val `val` = Math.acos(x)
    return radiansToDegrees(`val`)
}

// degree arctan
 fun darctan(x: Double): Double {
    val `val` = Math.atan(x)
    return radiansToDegrees(`val`)
}

// degree arctan2
 fun darctan2(y: Double, x: Double): Double {
    val `val` = Math.atan2(y, x)
    return radiansToDegrees(`val`)
}

// degree arccot
 fun darccot(x: Double): Double {
    val `val` = Math.atan2(1.0, x)
    return radiansToDegrees(`val`)
}
