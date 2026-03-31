package com.shruti.reminderapp.RoomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shruti.reminderapp.dataclass.Content


@Database(entities = [Reminder::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun dao() : ReminderDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase ?= null

        fun getDatabase(content: Context): AppDatabase {
            return INSTANCE?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    content.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}