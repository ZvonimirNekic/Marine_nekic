package org.unizd.rma.nekic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.unizd.rma.nekic.dao.TaskDao
import org.unizd.rma.nekic.models.Task


@Database(
    entities = [Task::class],
    version = 6,
)
@TypeConverters(org.unizd.rma.nekic.converter.TypeConverter::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract val taskDao : TaskDao

    companion object {

        private val migration_5_6 = object : Migration(5,6){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `Task_new` (" +
                        "`taskId` TEXT NOT NULL, " +
                        "`taskTitle` TEXT NOT NULL, " +
                        "`color` TEXT NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`depth` TEXT NOT NULL, " +
                        "`marineurl` TEXT NOT NULL, " +
                        "`imageUri` TEXT NOT NULL, " +
                        "`typeofmarine` TEXT NOT NULL, " +
                        "PRIMARY KEY(`taskId`))")

                database.execSQL("INSERT INTO `Task_new` (`taskId`, `taskTitle`, `color`, `date`, `depth`, `marineurl`, `imageUri`, `typeofmarine`) " +
                        "SELECT `taskId`, `taskTitle`, '', `date`, '', '', `imageUri`, '' FROM `Task`")

                database.execSQL("DROP TABLE `Task`")

                database.execSQL("ALTER TABLE `Task_new` RENAME TO `Task`")

            }
        }


        @Volatile
        private var INSTANCE: TaskDatabase? = null
        fun getInstance(context: Context): TaskDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_db"

                ).addMigrations(migration_5_6)
                    .build().also {
                    INSTANCE = it
                }
            }

        }
    }

}