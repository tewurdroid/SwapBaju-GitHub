package com.example.swapbaju

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

object ModelImporter {

    fun import(
        context: Context,
        uri: Uri,
        type: ModelType
    ): ImportedModel {
        return try {
            val fileName = queryName(context, uri) ?: "model.tflite"
            val bytes = context.contentResolver.openInputStream(uri)
                ?.use { it.readBytes() }
                ?: error("Tidak bisa membaca file model.")

            require(bytes.isNotEmpty()) { "File model kosong." }

            val buffer = ByteBuffer.allocateDirect(bytes.size)
                .order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()

            val interpreter = Interpreter(buffer)

            val inputs = (0 until interpreter.inputTensorCount)
                .map { interpreter.getInputTensor(it).shape().toList() }

            val outputs = (0 until interpreter.outputTensorCount)
                .map { interpreter.getOutputTensor(it).shape().toList() }

            interpreter.close()

            val model = ImportedModel(
                id = UUID.randomUUID().toString(),
                displayName = fileName,
                type = type,
                uri = uri.toString(),
                sizeBytes = bytes.size.toLong(),
                inputShapes = inputs,
                outputShapes = outputs,
                valid = true
            )

            ModelImportStore.save(context, model)
            model
        } catch (e: Exception) {
            ImportedModel(
                id = UUID.randomUUID().toString(),
                displayName = "Invalid model",
                type = type,
                uri = uri.toString(),
                sizeBytes = 0,
                inputShapes = emptyList(),
                outputShapes = emptyList(),
                valid = false,
                error = e.message ?: "Model tidak valid."
            )
        }
    }

    fun copyIntoPrivateStorage(
        context: Context,
        model: ImportedModel
    ): File? {
        return try {
            val source = Uri.parse(model.uri)
            val dir = File(context.filesDir, "models")
            dir.mkdirs()

            val safeName = model.id + "_" +
                model.displayName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val target = File(dir, safeName)

            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            target
        } catch (_: Exception) {
            null
        }
    }

    private fun queryName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }
}
