package com.example.firebase
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var captureButton: Button
    private lateinit var imageView: ImageView

    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    private val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_PERMISSION_CODE = 2

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        captureButton = findViewById(R.id.capture_button)
        imageView = findViewById(R.id.image_view)

        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        captureButton.setOnClickListener {
            // Check for camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                captureImage()
            }
        }
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storage.reference.child("images/$imageName")



            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnCompleteListener { task: Task<*> ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnCompleteListener { urlTask: Task<*> ->
                        if (urlTask.isSuccessful) {
                            val downloadUrl = urlTask.result.toString()
                            val image = hashMapOf(
                                "url" to downloadUrl,
                                "Latitute" to "dgfchvjbknyctgvjhbk",
                                "Long" to "12345678"

                            )
                            firestore.collection("images")
                                .add(image)
                                .addOnSuccessListener { documentReference ->
                                    // Show success message
                                }
                                .addOnFailureListener { e ->
                                    // Show error message
                                }
                        }

                    }
                } else {
                    // Show error message
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            captureImage()
        }
    }
}
