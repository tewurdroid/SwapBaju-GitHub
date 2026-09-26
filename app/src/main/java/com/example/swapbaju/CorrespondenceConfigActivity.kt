package com.example.swapbaju

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.swapbaju.databinding.ActivityCorrespondenceConfigBinding
import com.example.swapbaju.model.CorrespondenceRuntimeConfig
import com.example.swapbaju.model.CorrespondenceRuntimeConfigStore

class CorrespondenceConfigActivity : AppCompatActivity() {
    private lateinit var b: ActivityCorrespondenceConfigBinding

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        b = ActivityCorrespondenceConfigBinding.inflate(layoutInflater)
        setContentView(b.root)

        CorrespondenceRuntimeConfigStore.load(this)?.let {
            b.etWidth.setText(it.inputWidth.toString())
            b.etHeight.setText(it.inputHeight.toString())
            b.etOutWidth.setText(it.outputWidth.toString())
            b.etOutHeight.setText(it.outputHeight.toString())
            b.etInputMin.setText(it.inputRangeMin.toString())
            b.etInputMax.setText(it.inputRangeMax.toString())
            b.etXMin.setText(it.outputXMin.toString())
            b.etXMax.setText(it.outputXMax.toString())
            b.etYMin.setText(it.outputYMin.toString())
            b.etYMax.setText(it.outputYMax.toString())
            b.etCMin.setText(it.confidenceMin.toString())
            b.etCMax.setText(it.confidenceMax.toString())
        }

        b.btnSave.setOnClickListener {
            try {
                val c = CorrespondenceRuntimeConfig(
                    b.etWidth.i(), b.etHeight.i(),
                    b.etOutWidth.i(), b.etOutHeight.i(),
                    b.etInputMin.f(), b.etInputMax.f(),
                    b.etXMin.f(), b.etXMax.f(),
                    b.etYMin.f(), b.etYMax.f(),
                    b.etCMin.f(), b.etCMax.f()
                )
                require(c.inputWidth > 0 && c.inputHeight > 0)
                require(c.outputWidth > 0 && c.outputHeight > 0)
                require(c.inputRangeMax > c.inputRangeMin)
                require(c.outputXMax > c.outputXMin)
                require(c.outputYMax > c.outputYMin)
                require(c.confidenceMax > c.confidenceMin)
                CorrespondenceRuntimeConfigStore.save(this, c)
                Toast.makeText(this, "Konfigurasi disimpan.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Konfigurasi tidak valid.", Toast.LENGTH_LONG).show()
            }
        }

        b.btnClear.setOnClickListener {
            CorrespondenceRuntimeConfigStore.clear(this)
            finish()
        }
    }

    private fun EditText.i() = text.toString().trim().toInt()
    private fun EditText.f() = text.toString().trim().toFloat()
}
