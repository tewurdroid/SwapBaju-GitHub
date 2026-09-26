package com.example.swapbaju

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityHumanParsingSetupBinding
import com.example.swapbaju.ActiveModelStore
import com.example.swapbaju.model.HumanParsingContractInspector
import com.example.swapbaju.ImportedModelResolver
import com.example.swapbaju.ModelType

class HumanParsingSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHumanParsingSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHumanParsingSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInspect.setOnClickListener {
            inspect()
        }
        binding.btnConfigure.setOnClickListener {
            startActivity(Intent(this, HumanParsingConfigActivity::class.java))
        }

        inspect()
    }

    private fun inspect() {
        val active = ActiveModelStore.getActive(this, ModelType.HUMAN_PARSING)
        if (active == null) {
            binding.tvResult.text = "Belum ada model Human Parsing aktif."
            binding.btnConfigure.isEnabled = false
            return
        }

        val resolved = runCatching {
            ImportedModelResolver.resolve(this, active)
        }.getOrNull()

        if (resolved == null) {
            binding.tvResult.text = "Model aktif tidak dapat dibuka."
            binding.btnConfigure.isEnabled = false
            return
        }

        val suggestion = HumanParsingContractInspector.inspect(resolved.localFile)
        binding.tvResult.text = buildString {
            appendLine("Model: ${resolved.metadata.displayName}")
            appendLine("Ukuran: ${resolved.metadata.sizeBytes} bytes")
            appendLine()
            appendLine("Input: ${suggestion.inputWidth ?: "tidak terdeteksi"} × ${suggestion.inputHeight ?: "tidak terdeteksi"}")
            appendLine("Output classes: ${suggestion.outputClasses ?: "tidak terdeteksi"}")
            appendLine("Layout: ${suggestion.outputLayout ?: "tidak terdeteksi"}")
            appendLine()
            suggestion.notes.forEach { appendLine("• $it") }
            appendLine()
            appendLine("Nomor class pakaian tetap harus diambil dari dokumentasi model.")
        }
        binding.btnConfigure.isEnabled =
            suggestion.inputWidth != null &&
            suggestion.inputHeight != null &&
            suggestion.outputClasses != null &&
            suggestion.outputLayout == "[1,H,W,C]"
    }
}
