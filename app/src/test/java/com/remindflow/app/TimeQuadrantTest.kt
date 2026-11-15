package com.remindflow.app

import com.remindflow.app.domain.model.Reminder
import com.remindflow.app.domain.model.ReminderCategory
import com.remindflow.app.domain.model.TimeQuadrant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeQuadrantTest {

    @Test
    fun testQuadrantFromHour() {
        // Morning: 06:00 to 11:59
        assertEquals(TimeQuadrant.MORNING, TimeQuadrant.fromHour(6))
        assertEquals(TimeQuadrant.MORNING, TimeQuadrant.fromHour(8))
        assertEquals(TimeQuadrant.MORNING, TimeQuadrant.fromHour(11))

        // Afternoon: 12:00 to 17:59
        assertEquals(TimeQuadrant.AFTERNOON, TimeQuadrant.fromHour(12))
        assertEquals(TimeQuadrant.AFTERNOON, TimeQuadrant.fromHour(14))
        assertEquals(TimeQuadrant.AFTERNOON, TimeQuadrant.fromHour(17))

        // Evening: 18:00 to 21:59
        assertEquals(TimeQuadrant.EVENING, TimeQuadrant.fromHour(18))
        assertEquals(TimeQuadrant.EVENING, TimeQuadrant.fromHour(20))
        assertEquals(TimeQuadrant.EVENING, TimeQuadrant.fromHour(21))

        // Night: 22:00 to 05:59
        assertEquals(TimeQuadrant.NIGHT, TimeQuadrant.fromHour(22))
        assertEquals(TimeQuadrant.NIGHT, TimeQuadrant.fromHour(23))
        assertEquals(TimeQuadrant.NIGHT, TimeQuadrant.fromHour(0))
        assertEquals(TimeQuadrant.NIGHT, TimeQuadrant.fromHour(3))
        assertEquals(TimeQuadrant.NIGHT, TimeQuadrant.fromHour(5))
    }

    @Test
    fun testReminderSchedulingDays() {
        // Everyday (127)
        val everydayReminder = Reminder(
            title = "Daily Routine",
            hour = 8,
            minute = 0,
            quadrant = TimeQuadrant.MORNING,
            daysOfWeekMask = 127,
            category = ReminderCategory.HEALTH
        )
        for (day in 1..7) {
            assertTrue("Should be scheduled on day $day", everydayReminder.isScheduledForDay(day))
        }

        // Weekdays only (31: Mon=1, Tue=2, Wed=4, Thu=8, Fri=16 -> 1+2+4+8+16 = 31)
        val weekdayReminder = Reminder(
            title = "Work Standup",
            hour = 9,
            minute = 30,
            quadrant = TimeQuadrant.MORNING,
            daysOfWeekMask = 31,
            category = ReminderCategory.WORK
        )
        for (day in 1..5) {
            assertTrue("Should be scheduled on weekday $day", weekdayReminder.isScheduledForDay(day))
        }
        assertFalse("Should not be scheduled on Saturday", weekdayReminder.isScheduledForDay(6))
        assertFalse("Should not be scheduled on Sunday", weekdayReminder.isScheduledForDay(7))
    }

    @Test
    fun testFormattedTime() {
        val morningReminder = Reminder(
            title = "Breakfast",
            hour = 8,
            minute = 5,
            quadrant = TimeQuadrant.MORNING,
            category = ReminderCategory.HEALTH
        )
        assertEquals("8:05 AM", morningReminder.formattedTime)

        val midnightReminder = Reminder(
            title = "Midnight Routine",
            hour = 0,
            minute = 0,
            quadrant = TimeQuadrant.NIGHT,
            category = ReminderCategory.HABITS
        )
        assertEquals("12:00 AM", midnightReminder.formattedTime)

        val afternoonReminder = Reminder(
            title = "Lunch",
            hour = 13,
            minute = 45,
            quadrant = TimeQuadrant.AFTERNOON,
            category = ReminderCategory.PERSONAL
        )
        assertEquals("1:45 PM", afternoonReminder.formattedTime)
    }

    @Test
    fun testCategoryInventorySupport() {
        // Specific types like Medicine/Supplements support inventory counts
        assertTrue(ReminderCategory.HEALTH.supportsInventory)

        // General reminders and routine types do not have inventory counts
        assertFalse(ReminderCategory.PERSONAL.supportsInventory)
        assertFalse(ReminderCategory.WORK.supportsInventory)
        assertFalse(ReminderCategory.FITNESS.supportsInventory)
        assertFalse(ReminderCategory.HABITS.supportsInventory)
        assertFalse(ReminderCategory.CHORES.supportsInventory)
        assertFalse(ReminderCategory.HYDRATION.supportsInventory)
    }

    @Test
    fun testSingleModeReminder() {
        val singleReminder = Reminder(
            title = "Doctor Appointment",
            hour = 10,
            minute = 30,
            quadrant = TimeQuadrant.MORNING,
            scheduleMode = com.remindflow.app.domain.model.ScheduleMode.SINGLE,
            singleDate = "2026-11-05",
            category = ReminderCategory.HEALTH
        )

        // Matching date
        assertTrue("Should be scheduled on 2026-11-05 (Thursday)", singleReminder.isScheduledForDate("2026-11-05", 4))

        // Different dates should NOT trigger
        assertFalse("Should not be scheduled on 2026-11-04", singleReminder.isScheduledForDate("2026-11-04", 3))
        assertFalse("Should not be scheduled on 2026-11-06", singleReminder.isScheduledForDate("2026-11-06", 5))
        assertFalse("Should not be scheduled on next week same day", singleReminder.isScheduledForDate("2026-11-12", 4))
    }

    @Test
    fun testRecurringWithSpecificDaysAndEndDate() {
        // Tuesday (bit 2 = 2) and Thursday (bit 4 = 8) -> mask = 10
        val tueThuReminder = Reminder(
            title = "Gym Session",
            hour = 18,
            minute = 0,
            quadrant = TimeQuadrant.EVENING,
            scheduleMode = com.remindflow.app.domain.model.ScheduleMode.RECURRING,
            daysOfWeekMask = 10, // Tue + Thu
            hasEndDate = true,
            endDate = "2026-11-20",
            category = ReminderCategory.FITNESS
        )

        // Tuesday (day 2) before end date -> TRUE
        assertTrue("Should be scheduled on Tuesday 2026-11-10", tueThuReminder.isScheduledForDate("2026-11-10", 2))
        // Thursday (day 4) before end date -> TRUE
        assertTrue("Should be scheduled on Thursday 2026-11-12", tueThuReminder.isScheduledForDate("2026-11-12", 4))

        // Monday (day 1) -> FALSE
        assertFalse("Should not be scheduled on Monday", tueThuReminder.isScheduledForDate("2026-11-09", 1))
        // Wednesday (day 3) -> FALSE
        assertFalse("Should not be scheduled on Wednesday", tueThuReminder.isScheduledForDate("2026-11-11", 3))

        // After end date (2026-11-24 is Tuesday, but after 2026-11-20) -> FALSE
        assertFalse("Should not be scheduled after end date", tueThuReminder.isScheduledForDate("2026-11-24", 2))
    }

    @Test
    fun testPastDateRemindersVisibleWithBlankStartDate() {
        val dailyReminder = Reminder(
            title = "Daily Vitamin",
            hour = 8,
            minute = 0,
            quadrant = TimeQuadrant.MORNING,
            scheduleMode = com.remindflow.app.domain.model.ScheduleMode.RECURRING,
            startDate = "", // Existing reminders in database have blank startDate
            daysOfWeekMask = 127,
            category = ReminderCategory.HEALTH
        )

        // Must be scheduled for any past date matching recurrence (e.g. yesterday, 5 days ago)
        assertTrue("Must be scheduled for past date 2026-11-01", dailyReminder.isScheduledForDate("2026-11-01", 7))
        assertTrue("Must be scheduled for past date 2026-11-08", dailyReminder.isScheduledForDate("2026-11-08", 7))
        assertTrue("Must be scheduled for current date 2026-11-09", dailyReminder.isScheduledForDate("2026-11-09", 1))
    }

    @Test
    fun testEntityToDomainRetainsStartDateWithoutDynamicShift() {
        val entity = com.remindflow.app.data.local.entity.ReminderEntity(
            id = 1L,
            title = "Morning Coffee",
            hour = 7,
            minute = 30,
            quadrantName = "MORNING",
            startDate = "",
            categoryName = "PERSONAL",
            colorHex = 0xFF6366F1,
            iconKey = "CheckCircle"
        )

        val domain = entity.toDomain()
        assertEquals("startDate must remain empty string and not dynamically shift forward", "", domain.startDate)
        // With blank startDate, it remains scheduled for past dates
        assertTrue("Should be visible on past dates", domain.isScheduledForDate("2026-11-01", 7))
    }
}

