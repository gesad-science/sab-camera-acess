package database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dao.UserDao
import model.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var instanceDatabase: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase =
            instanceDatabase ?: synchronized(this) {
                Room
                    .databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "users_db",
                    ).fallbackToDestructiveMigration(true)
                    .build()
                    .also { instanceDatabase = it }
            }
    }
}
