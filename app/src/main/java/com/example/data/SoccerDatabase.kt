package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PlayerCard::class, UserStats::class], version = 1, exportSchema = false)
abstract class SoccerDatabase : RoomDatabase() {
    abstract fun soccerDao(): SoccerDao

    companion object {
        @Volatile
        private var INSTANCE: SoccerDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SoccerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoccerDatabase::class.java,
                    "soccer_database"
                )
                .addCallback(SoccerDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SoccerDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.soccerDao()
                    // Populate default players
                    dao.insertPlayers(listOfDefaultPlayers())
                    // Initialize empty UserStats
                    if (dao.getUserStats() == null) {
                        dao.insertUserStats(UserStats())
                    }
                }
            }
        }
    }
}
