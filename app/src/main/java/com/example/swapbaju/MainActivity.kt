package com.example.swapbaju

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.swapbaju.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var personBitmap: Bitmap? = null
    private var clothBitmap: Bitmap? = null
    private var processing = false

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("image_uri")?.let {
                BitmapHelper.loadBitmap(this, Uri.parse(it))?.let { bmp ->
                    personBitmap = BitmapHelper.resizeToSafeSize(bmp)
                    binding.imgPerson.setImageBitmap(personBitmap)
                }
            }
        }
    }

    private val clothPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            BitmapHelper.loadBitmap(this, it)?.let { bmp ->
                clothBitmap = BitmapHelper.resizeToSafeSize(bmp)
                binding.imgCloth.setImageBitmap(clothBitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateModelStatus()

        binding.btnPickPerson.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        binding.btnPickCloth.setOnClickListener {
            clothPicker.launch("image/*")
        }

        binding.btnImportModel.setOnClickListener {
            startActivity(Intent(this, ModelImportActivity::class.java))
        }

        binding.btnModelDiagnostics.setOnClickListener {
            startActivity(Intent(this, ModelDiagnosticsActivity::class.java))
        }

        binding.btnSwapBaju.setOnClickListener {
            processFitting()
        }

        binding.btnSaveResult.setOnClickListener {
            saveResult()
        }
    }



private fun updateModelStatus() {
    val availability = ModelAvailabilityChecker.check(this)
    val mode = ModelAvailabilityChecker.bestAvailable(this)

    binding.txtModelStatus.text = when {
        !availability.pose ->
            "Mode: Fallback — model pose belum tersedia"
        mode == VtonMode.FULL_VTON ->
            "Mode: Full VTON"
        mode == VtonMode.AI_HUMAN_PARSING ->
            "Mode: AI Human Parsing"
        else ->
            "Mode: Fallback"
    }
}

        private fun processFitting() {
        if (processing) return
        processing = true

        val person = personBitmap
            ?: return toast("Ambil foto orang terlebih dahulu.")

        val cloth = clothBitmap
            ?: return toast("Pilih foto baju terlebih dahulu.")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSwapBaju.isEnabled = false

        lifecycleScope.launch(Dispatchers.Default) {
            val detector = PoseDetector(this@MainActivity)

            try {
                val pose = detector.detect(person)
                val prepared = GarmentPreprocessor.prepare(cloth)

                val requestedMode = ModelAvailabilityChecker.bestAvailable(this@MainActivity)
                val bundle = VtonPipelineFactory.create(
                    context = this@MainActivity,
                    pose = pose,
                    requestedMode = requestedMode
                )
                val pipeline = bundle.pipeline

                val result = pipeline.run(
                    person = person,
                    garment = prepared.bitmap,
                    garmentSourceMask = prepared.mask,
                    pose = pose
                )

                withContext(Dispatchers.Main) {
                    binding.imgResult.setImageBitmap(result)
                    binding.progressBar.visibility = View.GONE
                    binding.btnSwapBaju.isEnabled = true
                    processing = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSwapBaju.isEnabled = true
                    processing = false
                    toast("Gagal: ${e.message ?: "kesalahan tidak diketahui"}")
                }
            } finally {
                detector.close()
            }
        }
    }

    private fun saveResult() {
        val drawable = binding.imgResult.drawable
            ?: return toast("Belum ada hasil untuk disimpan.")

        val output = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )

        val canvas = android.graphics.Canvas(output)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        lifecycleScope.launch(Dispatchers.IO) {
            val saved = ImageSaver.save(this@MainActivity, output)

            withContext(Dispatchers.Main) {
                toast(
                    if (saved) {
                        "Hasil tersimpan di Pictures/SwapBaju."
                    } else {
                        "Gagal menyimpan hasil."
                    }
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


override fun onDestroy() {
    personBitmap?.let { if (!it.isRecycled) it.recycle() }
    clothBitmap?.let { if (!it.isRecycled) it.recycle() }
    personBitmap = null
    clothBitmap = null
    super.onDestroy()
}
