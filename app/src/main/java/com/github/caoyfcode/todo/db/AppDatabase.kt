package com.github.caoyfcode.todo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.github.caoyfcode.todo.db.dao.GroupDao
import com.github.caoyfcode.todo.db.dao.TodoDao
import com.github.caoyfcode.todo.db.entity.Group
import com.github.caoyfcode.todo.db.entity.Todo
import java.time.LocalDateTime

@Database(entities = [Todo::class, Group::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "TodoApp.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun toDateTime(value: String?): LocalDateTime? {
        return value?.let {
            LocalDateTime.parse(it)
        }
    }

    @TypeConverter
    fun toDateTimeString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
}