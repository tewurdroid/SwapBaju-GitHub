package com.example.swapbaju

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityHumanParsingConfigBinding
import com.example.swapbaju.model.HumanParsingRuntimeConfigStore
import com.example.swapbaju.model.HumanParsingRuntimeConfig

class HumanParsingConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHumanParsingConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHumanParsingConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val existing = HumanParsingRuntimeConfigStore.load(this)
        if (existing != null) {
            binding.etWidth.setText(existing.inputWidth.toString())
            binding.etHeight.setText(existing.inputHeight.toString())
            binding.etClasses.setText(existing.outputClasses.toString())
            binding.etBackground.setText(existing.backgroundClass.toString())
            binding.etUpper.setText(existing.upperGarmentClasses.sorted().joinToString(","))
            binding.etLower.setText(existing.lowerGarmentClasses.sorted().joinToString(","))
            binding.etMean.setText(existing.mean.joinToString(","))
            binding.etStd.setText(existing.std.joinToString(","))
            binding.etThreshold.setText(existing.confidenceThreshold.toString())
        }

        binding.btnSave.setOnClickListener {
            save()
        }
        binding.btnClear.setOnClickListener {
            HumanParsingRuntimeConfigStore.clear(this)
            Toast.makeText(this, "Konfigurasi dihapus.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun save() {
        try {
            val width = binding.etWidth.text.toString().trim().toInt()
            val height = binding.etHeight.text.toString().trim().toInt()
            val classes = binding.etClasses.text.toString().trim().toInt()
            val background = binding.etBackground.text.toString().trim().toInt()
            val upper = parseInts(binding.etUpper.text.toString())
            val lower = parseInts(binding.etLower.text.toString())
            val mean = parseFloats(binding.etMean.text.toString(), 3)
            val std = parseFloats(binding.etStd.text.toString(), 3)
            val threshold = binding.etThreshold.text.toString().trim().toFloat()

            require(width > 0 && height > 0 && classes > 1)
            require(threshold in 0f..1f)
            require(std.all { it != 0f })
            require(upper.all { it in 0 until classes })
            require(lower.all { it in 0 until classes })
            require(background in 0 until classes)

            HumanParsingRuntimeConfigStore.save(
                this,
                HumanParsingRuntimeConfig(
                    inputWidth = width,
                    inputHeight = height,
                    outputClasses = classes,
                    backgroundClass = background,
                    upperGarmentClasses = upper,
                    lowerGarmentClasses = lower,
                    mean = mean,
                    std = std,
                    confidenceThreshold = threshold
                )
            )
            Toast.makeText(this, "Konfigurasi disimpan.", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Konfigurasi tidak valid: ${e.message ?: "periksa semua field"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun parseInts(raw: String): Set<Int> =
        raw.split(",", " ", "\n", "\t")
            .mapNotNull { it.trim().takeIf(String::isNotEmpty)?.toIntOrNull() }
            .toSet()

    private fun parseFloats(raw: String, count: Int): FloatArray {
        val values = raw.split(",", " ", "\n", "\t")
            .mapNotNull { it.trim().takeIf(String::isNotEmpty)?.toFloatOrNull() }
        require(values.size == count) { "Mean/std harus berisi $count angka." }
        return values.toFloatArray()
    }
}
