package org.unizd.rma.nekic.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.unizd.rma.nekic.models.Task
import org.unizd.rma.nekic.repository.TaskRepository
import org.unizd.rma.nekic.utils.Resource

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskRepository = TaskRepository(application)

    fun getTaskList() = taskRepository.getTaskList()

    fun insertTask(task: Task) : MutableLiveData<Resource<Long>>{
      return taskRepository.insertTask(task) as MutableLiveData<Resource<Long>>
    }

    fun deleteTaskUsingId(taskId: String) : MutableLiveData<Resource<Int>>{
        return taskRepository.deleteTaskUsingId(taskId) as MutableLiveData<Resource<Int>>
    }

    fun updateTask(task: Task) : MutableLiveData<Resource<Int>>{
        return taskRepository.updateTask(task) as MutableLiveData<Resource<Int>>
    }

}