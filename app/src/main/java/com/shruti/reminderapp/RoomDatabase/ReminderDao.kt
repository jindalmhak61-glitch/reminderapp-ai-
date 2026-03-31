package com.shruti.reminderapp.RoomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface ReminderDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Query("SELECT * FROM Reminders")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE date = :selectedDate")
    fun getRemindersByDate(selectedDate: String): List<Reminder>

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)
}