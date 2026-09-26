package com.example.swapbaju

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityModelImportBinding

class ModelImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelImportBinding

    private val picker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult

        val type = when (binding.spinnerModelType.selectedItemPosition) {
            0 -> ModelType.HUMAN_PARSING
            1 -> ModelType.CORRESPONDENCE
            else -> ModelType.REFINEMENT
        }

        contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val result = ModelImporter.import(this, uri, type)

        if (result.valid) {
                ActiveModelStore.setActive(this, type, result.id)
            }

            binding.txtImportResult.text =
            if (result.valid) {
                buildString {
                    append("Model valid\n")
                    append("Nama: ${result.displayName}\n")
                    append("Ukuran: ${result.sizeBytes / 1024} KB\n")
                    append("Input: ${result.inputShapes}\n")
                    append("Output: ${result.outputShapes}")
                }
            } else {
                "Model tidak valid: ${result.error}"
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModelImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spinnerModelType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Human Parsing",
                "Dense Correspondence",
                "Image Refinement"
            )
        )

        binding.btnChooseModel.setOnClickListener {
            picker.launch(arrayOf(
                "application/octet-stream",
                "application/x-tflite",
                "*/*"
            ))
        }
    }
}
