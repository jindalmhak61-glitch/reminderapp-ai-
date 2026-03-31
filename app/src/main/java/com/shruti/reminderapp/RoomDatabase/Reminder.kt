package com.shruti.reminderapp.RoomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String?= null,
    val date: String?= null,
    val time: String?= null

)
