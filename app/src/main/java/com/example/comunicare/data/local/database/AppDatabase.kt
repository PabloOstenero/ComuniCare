package com.example.comunicare.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.comunicare.data.local.dao.ChatMessageDao
import com.example.comunicare.data.local.dao.HelpRequestDao
import com.example.comunicare.data.local.dao.UserDao
import com.example.comunicare.data.local.entity.ChatMessageEntity
import com.example.comunicare.data.local.entity.HelpRequestEntity
import com.example.comunicare.data.local.entity.UserEntity

@Database(
    entities = [HelpRequestEntity::class, ChatMessageEntity::class, UserEntity::class],
    version = 10, // Subimos a la versión 10 para garantizar un esquema limpio
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun helpRequestDao(): HelpRequestDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comunicare_db_v10" // Nuevo nombre para evitar conflictos previos
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
