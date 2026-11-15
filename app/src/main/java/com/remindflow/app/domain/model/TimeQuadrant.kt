package com.remindflow.app.domain.model

enum class TimeQuadrant(
    val title: String,
    val defaultStartHour: Int,
    val defaultEndHour: Int,
    val iconName: String,
    val colorHex: Long
) {
    MORNING("Morning", 6, 12, "WbSunny", 0xFFF59E0B),       // Amber/Gold
    AFTERNOON("Afternoon", 12, 18, "LightMode", 0xFF06B6D4), // Cyan/Sky
    EVENING("Evening", 18, 22, "NightsStay", 0xFF8B5CF6),    // Purple
    NIGHT("Night", 22, 6, "Bedtime", 0xFF3B82F6);           // Deep Indigo/Blue

    companion object {
        fun fromHour(hour: Int): TimeQuadrant {
            return when (hour) {
                in 6..11 -> MORNING
                in 12..17 -> AFTERNOON
                in 18..21 -> EVENING
                else -> NIGHT
            }
        }
    }
}
