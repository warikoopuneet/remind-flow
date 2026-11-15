package com.remindflow.app.domain.model

enum class ReminderCategory(
    val displayName: String,
    val iconKey: String,
    val colorHex: Long,
    val supportsInventory: Boolean = false
) {
    HEALTH("Health & Meds", "Medication", 0xFFEC4899, supportsInventory = true),       // Medicines, vitamins, prescriptions
    FITNESS("Fitness & Exercise", "FitnessCenter", 0xFF10B981, supportsInventory = false), // Workouts, walks
    HYDRATION("Hydration", "WaterDrop", 0xFF0EA5E9, supportsInventory = false),          // Drinking water
    WORK("Work & Focus", "Laptop", 0xFF6366F1, supportsInventory = false),              // Standups, tasks
    HABITS("Habits & Mindset", "SelfImprovement", 0xFF8B5CF6, supportsInventory = false),  // Reading, meditation
    CHORES("Chores & Home", "CleaningServices", 0xFFF97316, supportsInventory = false), // Cleaning, plants
    PERSONAL("General Reminder", "Person", 0xFF14B8A6, supportsInventory = false);       // General tasks

    companion object {
        fun fromName(name: String): ReminderCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: PERSONAL
        }
    }
}
