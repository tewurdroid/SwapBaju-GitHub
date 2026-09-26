package com.example.swapbaju

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityVtonReadinessBinding
import com.example.swapbaju.model.VtonRuntimeReadinessChecker

class VtonReadinessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVtonReadinessBinding

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        binding = ActivityVtonReadinessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCheck.setOnClickListener {
            refresh()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) refresh()
    }

    private fun refresh() {
        val result = VtonRuntimeReadinessChecker.check(this)
        val resolved = com.example.swapbaju.model.VtonModeResolver.resolve(this)
        binding.tvResult.text = result.summary() +
            "\n\nPipeline mode yang akan dipakai: ${resolved.mode}\n${resolved.reason}"
    }
}
