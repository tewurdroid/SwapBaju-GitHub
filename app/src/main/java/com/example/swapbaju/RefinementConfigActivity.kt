package com.example.swapbaju

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityRefinementConfigBinding
import com.example.swapbaju.model.RefinementRuntimeConfig
import com.example.swapbaju.model.RefinementRuntimeConfigStore

class RefinementConfigActivity : AppCompatActivity() {
    private lateinit var b: ActivityRefinementConfigBinding

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        b = ActivityRefinementConfigBinding.inflate(layoutInflater)
        setContentView(b.root)

        RefinementRuntimeConfigStore.load(this)?.let {
            b.etWidth.setText(it.inputWidth.toString())
            b.etHeight.setText(it.inputHeight.toString())
            b.etInputMin.setText(it.inputMin.toString())
            b.etInputMax.setText(it.inputMax.toString())
            b.etOutputMin.setText(it.outputMin.toString())
            b.etOutputMax.setText(it.outputMax.toString())
        }

        b.btnSave.setOnClickListener {
            try {
                val c = RefinementRuntimeConfig(
                    b.etWidth.i(), b.etHeight.i(),
                    b.etInputMin.f(), b.etInputMax.f(),
                    b.etOutputMin.f(), b.etOutputMax.f()
                )
                require(c.inputWidth > 0 && c.inputHeight > 0)
                require(c.inputMax > c.inputMin)
                require(c.outputMax > c.outputMin)
                RefinementRuntimeConfigStore.save(this, c)
                Toast.makeText(this, "Konfigurasi disimpan.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (_: Exception) {
                Toast.makeText(this, "Konfigurasi tidak valid.", Toast.LENGTH_LONG).show()
            }
        }

        b.btnClear.setOnClickListener {
            RefinementRuntimeConfigStore.clear(this)
            finish()
        }
    }

    private fun EditText.i() = text.toString().trim().toInt()
    private fun EditText.f() = text.toString().trim().toFloat()
}
