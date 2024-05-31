package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifier: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    companion object {
        const val IMAGE_URI = "image_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifier = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            analyzeImage()
        }
    }

    private val PICK_IMAGE_REQUEST = 1

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            currentImageUri = data.data
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.previewImageView.setImageURI(uri)
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            imageClassifier.classifyStaticImage(uri)
        } ?: showToast("Please select an image first.")
    }

    override fun onError(error: String) {
        showToast(error)
    }

    override fun onResults(result: List<Classifications>?, inferenceTime: Long) {
        if (result != null && result.isNotEmpty()) {
            val score = result[0].categories[0].score * 100
            val formattedScore = DecimalFormat("##").format(score)
            val resultText = "${result[0].categories[0].label}: $formattedScore%"
            moveToResult(resultText)
        } else {
            showToast("No result")
        }
    }

    private fun moveToResult(resultText: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(IMAGE_URI, currentImageUri.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT_TEXT, resultText)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}