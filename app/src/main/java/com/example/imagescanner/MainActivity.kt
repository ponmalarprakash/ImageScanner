package com.example.imagescanner

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition


private const val CAMERA_REQUEST = 1888;

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private var text: TextView? = null
    private var image: ImageView? = null
    private val mGraphicOverlay: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById<View>(R.id.btnCaptureImage) as Button
        text = findViewById<View>(R.id.textView) as TextView
        image = findViewById<View>(R.id.image) as ImageView
    }

    override fun onResume() {
        super.onResume()
        button.setOnClickListener {
            val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(i, CAMERA_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST) {
            val photo = data?.extras!!["data"] as Bitmap?
            if (photo != null) {
                image?.setImageBitmap(photo)
                runTextRecognition(InputImage.fromBitmap(photo, 0))
            }
        }
    }

    private fun runTextRecognition(image: InputImage) {
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun processTextRecognitionResult(texts: com.google.mlkit.vision.text.Text?) {
        val blocks: List<com.google.mlkit.vision.text.Text.TextBlock> = texts!!.textBlocks
        if (blocks.isEmpty()) {
            Log.e("resultText: ", "Empty")
        } else {
            var scannedText = StringBuilder()
            for (block in texts.textBlocks) {
                val blockText = block.text
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox
                for (line in block.lines) {
                    val lineText = line.text
                    val lineCornerPoints = line.cornerPoints
                    val lineFrame = line.boundingBox
                    for (element in line.elements) {
                        val elementText = element.text
                        val elementCornerPoints = element.cornerPoints
                        val elementFrame = element.boundingBox
                        scannedText.append("$elementText  \n")
                    }
                }
            }
            text?.text = scannedText.toString()
            /* var scannedText = StringBuilder()
             for (i in blocks.indices) {
                 val lines = blocks[i].lines
                 for (j in lines.indices) {
                     val elements = lines[j].elements
                     for (k in elements.indices) {
                         val textGraphic = elements[k].text
                         mGraphicOverlay.add(textGraphic)
                         scannedText.append("$textGraphic \n")
                     }
                 }
             }
             text?.text = scannedText.toString()*/
        }
    }
}