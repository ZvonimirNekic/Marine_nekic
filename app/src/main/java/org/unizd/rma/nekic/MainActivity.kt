package org.unizd.rma.nekic

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.Button

import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.unizd.rma.nekic.adapter.TaskViewAdapter
import org.unizd.rma.nekic.databinding.ActivityMainBinding
import org.unizd.rma.nekic.models.Task
import org.unizd.rma.nekic.utils.Status
import org.unizd.rma.nekic.utils.clearEditText
import org.unizd.rma.nekic.utils.longToastShow
import org.unizd.rma.nekic.utils.setupDialog
import org.unizd.rma.nekic.utils.validateEditText
import org.unizd.rma.nekic.viewmodels.TaskViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var captureIV: ImageView
    private lateinit var captureIV2: ImageView
    private lateinit var imageUri: Uri

    private lateinit var radioButtonT1: RadioButton
    private lateinit var radioButtonT2: RadioButton
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        captureIV.setImageURI(null)
        captureIV.setImageURI(imageUri)
        captureIV2.setImageURI(null)
        captureIV2.setImageURI(imageUri)

        saveImageUriToSharedPreferences(imageUri)
    }

    private val mainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }


    private val addTaskDialog: Dialog by lazy {
        Dialog(this).apply {
            setupDialog(R.layout.add_task_dialog)
        }
    }

    private val updateTaskDialog: Dialog by lazy {
        Dialog(this).apply {
            setupDialog(R.layout.update_task_dialog)
        }
    }


    private val loadingDialog: Dialog by lazy {
        Dialog(this).apply {
            setupDialog(R.layout.loading_dialog)
        }
    }

    private val taskViewModel: TaskViewModel by lazy {
        ViewModelProvider(this)[TaskViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Add task start
        val addCloseImg = addTaskDialog.findViewById<ImageView>(R.id.closeImg)
        addCloseImg.setOnClickListener { addTaskDialog.dismiss() }

        val addETedColor = addTaskDialog.findViewById<TextInputEditText>(R.id.edcolor)
        val addETedColorL = addTaskDialog.findViewById<TextInputLayout>(R.id.edcolorL)

        imageUri = retrieveImageUriFromSharedPreferences() ?: createImageUri()

        addETedColor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETedColor, addETedColorL)
            }

        })


        val addETedDepth = addTaskDialog.findViewById<TextInputEditText>(R.id.edDepth)
        val addETedDepthL = addTaskDialog.findViewById<TextInputLayout>(R.id.edDepthL)

        // InputFilter to allow only numeric input for addETDesc
        addETedDepth.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(this, "Only numeric input is allowed", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        })

        addETedDepth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETedDepth, addETedDepthL)
            }
        })


        val addETedDesignation = addTaskDialog.findViewById<TextInputEditText>(R.id.eddesignation)
        val addETedDesignationL = addTaskDialog.findViewById<TextInputLayout>(R.id.eddesignationL)

        addETedDesignation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(addETedDesignation, addETedDesignationL)
            }
        })

        imageUri = createImageUri()
        captureIV = addTaskDialog.findViewById(R.id.selectedImageView)
        val captureImgBtn = addTaskDialog.findViewById<Button>(R.id.pickImageButton)
        captureImgBtn.setOnClickListener {
            imageUri = createImageUri()
            contract.launch(imageUri)
        }
        imageUri = createImageUri()
        captureIV2 = updateTaskDialog.findViewById(R.id.upselectedImageView)
        val upCaptureBtn = updateTaskDialog.findViewById<Button>(R.id.updatepickImageButton)

        upCaptureBtn.setOnClickListener {
            imageUri = createImageUri()
            contract.launch(imageUri)
        }
        mainBinding.addTaskFABtn.setOnClickListener {
            clearEditText(addETedColor, addETedColorL)
            clearEditText(addETedDepth, addETedDepthL)
            clearEditText(addETedDesignation, addETedDesignationL)
            imageUri = createImageUri()
            captureIV.setImageURI(null) // Clear the ImageView
            addTaskDialog.show()
        }
        val saveTaskBtn = addTaskDialog.findViewById<Button>(R.id.saveBtn)
        saveTaskBtn.setOnClickListener {

            radioButtonT1 = addTaskDialog.findViewById(R.id.radioButtonFish)
            radioButtonT2 = addTaskDialog.findViewById(R.id.radioButtonCorals)

            val typeOfMarine = if (radioButtonT1.isChecked) {
                "Fish"
            } else {
                "Corals"
            }

            if (validateEditText(addETedColor, addETedColorL) && validateEditText(
                    addETedDepth,
                    addETedDepthL
                ) && validateEditText(addETedDesignation, addETedDesignationL)
            ) {
                addTaskDialog.dismiss()

                val newTask = Task(
                    UUID.randomUUID().toString(),
                    addETedDesignation.text.toString().trim(),
                    addETedColor.text.toString().trim(),
                    Date(), // This line represents the date
                    addETedDepth.text.toString().trim(),
                    imageUri.toString(),
                    typeOfMarine = typeOfMarine
                )

                taskViewModel.insertTask(newTask).observe(this) {

                    when (it.status) {

                        Status.LOADING -> {
                            loadingDialog.show()

                        }

                        Status.SUCCESS -> {

                            loadingDialog.dismiss()
                            if (it.data?.toInt() != -1) {
                                longToastShow("Added Successfully")

                            }

                        }

                        Status.ERROR -> {

                            loadingDialog.dismiss()
                            it.message?.let { it1 -> longToastShow(it1) }

                        }

                    }
                }

            }
        }


        val updateETTcolor = updateTaskDialog.findViewById<TextInputEditText>(R.id.updateedcolor)
        val updateETcolorL = updateTaskDialog.findViewById<TextInputLayout>(R.id.updateedcolorL)

        updateETTcolor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateETTcolor, updateETcolorL)
            }

        })

        val updateedDepth = updateTaskDialog.findViewById<TextInputEditText>(R.id.updateedDepth)
        val updateedDepthL = updateTaskDialog.findViewById<TextInputLayout>(R.id.updateedDepthL)

        // InputFilter to allow only numeric input for addETDesc
        updateedDepth.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(this, "Only numeric input is allowed", Toast.LENGTH_SHORT).show()
                    return@InputFilter ""
                }
            }
            null
        })
        updateedDepth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateedDepth, updateedDepthL)
            }
        })


        val updateedDesignation = updateTaskDialog.findViewById<TextInputEditText>(R.id.updateeddesignation)
        val updateedDesignationL = updateTaskDialog.findViewById<TextInputLayout>(R.id.updateeddesignationL)

        updateedDesignation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEditText(updateedDesignation, updateedDesignationL)
            }
        })

        val updateCloseImg = updateTaskDialog.findViewById<ImageView>(R.id.updatecloseImg)
        updateCloseImg.setOnClickListener { updateTaskDialog.dismiss() }

        val updateTaskBtn = updateTaskDialog.findViewById<Button>(R.id.updateBtn)


        val taskViewAdapter = TaskViewAdapter {

                type, position, task ->

            if (type == "delete") {

                taskViewModel
                    .deleteTaskUsingId(task.id).observe(this) {

                        when (it.status) {

                            Status.LOADING -> {
                                loadingDialog.show()

                            }

                            Status.SUCCESS -> {

                                loadingDialog.dismiss()
                                if (it.data != -1) {
                                    longToastShow(" Deleted Successfully")

                                }

                            }

                            Status.ERROR -> {

                                loadingDialog.dismiss()
                                it.message?.let { it1 -> longToastShow(it1) }

                            }

                        }


                    }
            } else if (type == "update") {
                captureIV2.setImageURI(Uri.parse(task.imageUri))
                updateETTcolor.setText(task.color)
                updateedDepth.setText(task.depth)
                updateedDesignation.setText(task.title)
                imageUri = Uri.parse(task.imageUri)


                // Initialize radio buttons
                radioButtonT1 = updateTaskDialog.findViewById(R.id.updateradioGroupMarinetype1)
                radioButtonT2 = updateTaskDialog.findViewById(R.id.updateradioGroupMarinetype2)

                if (task.typeOfMarine == "Fish"){
                    radioButtonT1.isChecked = true
                    radioButtonT2.isChecked = false
                }else{
                    radioButtonT1.isChecked = false
                    radioButtonT2.isChecked = true
                }

                updateTaskBtn.setOnClickListener {
                    // Get the new image URI
                    val newImageUri = if (imageUri == null) Uri.parse(task.imageUri) else imageUri
                    // Get the selected option from radio buttons
                    val selectedTypeOfMarine = if (radioButtonT1.isChecked) {
                        "Fish"
                    } else {
                        "Corals"
                    }
                    if (validateEditText(updateETTcolor, updateETcolorL) && validateEditText(
                            updateedDepth,
                            updateedDepthL
                        ) && validateEditText(updateedDesignation, updateedDesignationL)
                    ) {

                        val updateTask = Task(
                            task.id,
                            updateedDesignation.text.toString().trim(),
                            updateETTcolor.text.toString().trim(),
                            Date(), // This line represents the date
                            updateedDepth.text.toString().trim(),
                            newImageUri.toString(),
                            typeOfMarine = selectedTypeOfMarine

                        )
                        updateTaskDialog.dismiss()
                        loadingDialog.show()
                        taskViewModel.updateTask(updateTask)
                            .observe(this) {

                                when (it.status) {

                                    Status.LOADING -> {
                                        loadingDialog.show()

                                    }

                                    Status.SUCCESS -> {

                                        loadingDialog.dismiss()
                                        if (it.data != -1) {
                                            longToastShow("Updated Successfully")

                                        }

                                    }

                                    Status.ERROR -> {

                                        loadingDialog.dismiss()
                                        it.message?.let { it1 -> longToastShow(it1) }

                                    }

                                }


                            }
                    }

                }

                updateTaskDialog.show()

            }

        }


        mainBinding.taskRV.adapter = taskViewAdapter

        callGetTaskList(taskViewAdapter)
    }

    private fun saveImageUriToSharedPreferences(uri: Uri) {
        val editor = sharedPreferences.edit()
        editor.putString("imageUri", uri.toString())
        editor.apply()
    }

    private fun retrieveImageUriFromSharedPreferences(): Uri? {
        val uriString = sharedPreferences.getString("imageUri", null)
        return uriString?.let { Uri.parse(it) }
    }
    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "camera_photos_$timeStamp.png"
        val image = File(filesDir, imageFileName)
        return FileProvider.getUriForFile(this, "org.unizd.rma.nekic.FileProvider", image)
    }

    private fun callGetTaskList(taskViewAdapter: TaskViewAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            taskViewModel.getTaskList().collect {
                when (it.status) {
                    Status.LOADING -> {
                        loadingDialog.show()
                    }
                    Status.SUCCESS -> {
                        it.data?.collect { taskList ->
                            loadingDialog.dismiss()
                            taskViewAdapter.addAllTask(taskList)
                        }
                    }
                    Status.ERROR -> {

                        loadingDialog.dismiss()
                        it.message?.let { it1 -> longToastShow(it1) }
                    }
                }
            }
        }
    }
}