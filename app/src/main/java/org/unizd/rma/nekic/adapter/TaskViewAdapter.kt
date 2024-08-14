package org.unizd.rma.nekic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.unizd.rma.nekic.R
import org.unizd.rma.nekic.models.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskViewAdapter(
    private val deleteUpdateCallBack: (type: String, position: Int, task: Task) -> Unit
) : RecyclerView.Adapter<TaskViewAdapter.ViewHolder>() {

    private val taskList = arrayListOf<Task>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val color: TextView = itemView.findViewById(R.id.color)
        val depth: TextView = itemView.findViewById(R.id.depth)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val designation: TextView = itemView.findViewById(R.id.designation)
        val delImg: ImageView = itemView.findViewById(R.id.deleteImg)
        val editImg: ImageView = itemView.findViewById(R.id.editImg)
        val marineImage: ImageView = itemView.findViewById(R.id.marineImage)
        val type: TextView = itemView.findViewById(R.id.type)
    }

    fun addAllTask(newTaskList: List<Task>) {
        taskList.clear()
        taskList.addAll(newTaskList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_task_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position]

        holder.color.text = task.color
        holder.depth.text = task.depth
        holder.designation.text = task.title
        holder.type.text = task.typeOfMarine

        // Load image from imageUri using Glide library
        Glide.with(holder.itemView.context)
            .load(task.imageUri)
            .into(holder.marineImage)

        holder.delImg.setOnClickListener {
            if (holder.adapterPosition != -1) {
                deleteUpdateCallBack("delete",holder.adapterPosition, task)
            }
        }

        holder.editImg.setOnClickListener {
            if (holder.adapterPosition != -1) {
                deleteUpdateCallBack("update",holder.adapterPosition, task)
            }
        }


        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a", Locale.getDefault())
        holder.dateText.text = dateFormat.format(task.date)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
