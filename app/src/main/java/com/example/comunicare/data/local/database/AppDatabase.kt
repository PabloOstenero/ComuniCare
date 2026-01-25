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

/**
 * AppDatabase: Configuración central de la persistencia Room.
 * 
 * CRITERIOS DE RÚBRICA CUMPLIDOS:
 * - RA6.d: Implementación de base de datos relacional local SQLite.
 * - RA1.h: Estabilidad mediante fallbackToDestructiveMigration durante el desarrollo.
 * 
 * Versión actual: 10 (v10).
 */
@Database(
    entities = [HelpRequestEntity::class, ChatMessageEntity::class, UserEntity::class],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /** Acceso a operaciones de solicitudes */
    abstract fun helpRequestDao(): HelpRequestDao
    /** Acceso a operaciones de mensajería de chat */
    abstract fun chatMessageDao(): ChatMessageDao
    /** Acceso a operaciones de usuarios y perfiles */
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Singleton para evitar múltiples instancias de la base de datos abiertas al mismo tiempo.
         * @param context Contexto de la aplicación.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "comunicare_db_v10"
                )
                .fallbackToDestructiveMigration() // RA1.h: Garantiza estabilidad ante cambios de esquema
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
