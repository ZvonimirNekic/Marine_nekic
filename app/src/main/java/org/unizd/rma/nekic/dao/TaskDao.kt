package org.unizd.rma.nekic.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.unizd.rma.nekic.models.Task


@Dao
interface TaskDao {


   @Query("SELECT * FROM Task ORDER BY date DESC")
    fun getTaskList() : Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Delete
    suspend fun deleteTask(task: Task) : Int

    @Query("DELETE FROM Task WHERE taskId == :taskId")
    suspend fun deleteTaskUsingId(taskId: String) : Int

    @Update
    suspend fun updateTask(task: Task): Int

    @Query("UPDATE Task SET taskTitle=:title, color = :color  , depth = :depth, imageUri = :imageUri   WHERE taskId = :taskId")
    suspend fun updateTaskPaticularField(taskId:String,title:String,color:String, depth: String, imageUri: String): Int
}