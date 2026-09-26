package com.example.swapbaju.model

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.swapbaju.databinding.ActivityPoseModelImportBinding
import java.io.File

class PoseModelImportActivity : Activity() {
    private lateinit var binding: ActivityPoseModelImportBinding
    private val picker = 7101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPoseModelImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refresh()

        binding.btnPickPose.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/octet-stream"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, picker)
        }

        binding.btnClearPose.setOnClickListener {
            PoseModelStore.clear(this)
            refresh()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != picker || resultCode != RESULT_OK) return

        val uri = data?.data ?: return
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                val temp = File(cacheDir, "pose_import_${System.currentTimeMillis()}.task")
                temp.outputStream().use { output -> input.copyTo(output) }
                if (temp.length() <= 0L) error("File kosong.")
                PoseModelStore.save(this, temp)
                temp.delete()
            } ?: error("File tidak dapat dibaca.")
        }.onSuccess {
            Toast.makeText(this, "Model pose berhasil dipasang.", Toast.LENGTH_SHORT).show()
            refresh()
        }.onFailure {
            Toast.makeText(this, "Gagal memasang: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refresh() {
        val status = PoseRuntimeStatusChecker.check(this)
        binding.tvPoseStatus.text =
            "Sumber: ${status.source}\nUkuran: ${status.sizeBytes} bytes\n\n${status.message}"
    }
}
