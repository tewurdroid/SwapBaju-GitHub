package com.example.swapbaju

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityModelDiagnosticsBinding
import com.example.swapbaju.model.PoseRuntimeStatusChecker

class ModelDiagnosticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModelDiagnosticsBinding


private fun buildReport(): String {
    val base = ModelManager.summary(this)
    val imported = ModelImportStore.load(this)

    val active = ModelType.entries.joinToString("\n") { type ->
        val id = ActiveModelStore.getActiveId(this, type)
        val name = imported.firstOrNull { it.id == id }?.displayName
        "${type.name}: ${name ?: "tidak ada"}"
    }

    return "$base\n\nMODEL IMPORTED AKTIF\n$active"
}

override fun onCreate
(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val poseStatus = PoseRuntimeStatusChecker.check(this)
        binding = ActivityModelDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnVtonReadiness.setOnClickListener {
            startActivity(android.content.Intent(this, VtonReadinessActivity::class.java))
        }
        binding.btnRefinementConfig.setOnClickListener {
            startActivity(android.content.Intent(this, RefinementConfigActivity::class.java))
        }
        binding.btnHumanParsingSetup.setOnClickListener {
            startActivity(android.content.Intent(this, HumanParsingSetupActivity::class.java))
        }
        binding.btnHumanParsingConfig.setOnClickListener {
            startActivity(android.content.Intent(this, HumanParsingConfigActivity::class.java))
        }

        binding.txtModelReport.text = buildReport()

        binding.btnRefreshModels.setOnClickListener {
            binding.txtModelReport.text = buildReport()
        }
    }
}
