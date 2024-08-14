package org.unizd.rma.nekic.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.unizd.rma.nekic.dao.TaskDao
import org.unizd.rma.nekic.database.TaskDatabase
import org.unizd.rma.nekic.models.Task
import org.unizd.rma.nekic.utils.Resource

class TaskRepository(application: Application) {
 private  val taskDao: TaskDao = TaskDatabase.getInstance(application).taskDao

    fun getTaskList() = flow{

    emit(Resource.Loading())

        try {

            val result = taskDao.getTaskList()
            emit(Resource.Success(result))

        }catch (e: Exception){
            emit(Resource.Error(e.message.toString()))
        }

    }

    fun insertTask(task: Task): Any = MutableLiveData<Resource<Long>> ().apply{
        postValue(Resource.Loading())

     try {

      CoroutineScope(Dispatchers.IO).launch {
       val result = taskDao.insertTask(task)
      postValue(Resource.Success(result))
      }

     } catch (e : Exception){
  postValue(Resource.Error(e.message.toString()))

     }
    }

    fun deleteTaskUsingId(taskId : String): Any = MutableLiveData<Resource<Int>> ().apply{
        postValue(Resource.Loading())

        try {

            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.deleteTaskUsingId(taskId)
                postValue(Resource.Success(result))
            }

        } catch (e : Exception){
            postValue(Resource.Error(e.message.toString()))

        }
    }


    fun updateTask(task: Task): Any = MutableLiveData<Resource<Int>> ().apply{
        postValue(Resource.Loading())

        try {

            CoroutineScope(Dispatchers.IO).launch {
                val result = taskDao.updateTask(task)
                postValue(Resource.Success(result))
            }

        } catch (e : Exception){
            postValue(Resource.Error(e.message.toString()))

        }
    }

}