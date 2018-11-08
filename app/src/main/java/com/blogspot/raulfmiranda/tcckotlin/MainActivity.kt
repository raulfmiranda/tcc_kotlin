package com.blogspot.raulfmiranda.tcckotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST = 1888
    private val maxWidthHeight = 500
    private var arquivoFoto: File? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTakePicture.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_CODE)

            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                arquivoFoto = File(geraCaminhoFoto())
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto))
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }
        }

        btnUploadPicture.setOnClickListener {
            uploadFoto()
        }
    }

    private fun uploadFoto() {
        arquivoFoto?.let {
            val storageReference = FirebaseStorage.getInstance().getReference().child(it.name)
            val uploadTask = storageReference.putFile(Uri.fromFile(it))

            uploadTask.addOnFailureListener {

            }.addOnSuccessListener {

            }

        }

    }

    private fun geraCaminhoFoto(): String? {
        val caminho = getExternalFilesDir(null)?.path + "/" + System.currentTimeMillis() + ".png"
        return caminho
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == CAMERA_PERMISSION_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Permissão da Câmera Permitida", Toast.LENGTH_SHORT).show()
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                arquivoFoto = File(geraCaminhoFoto())
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto))
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            } else {
                Toast.makeText(this@MainActivity, "Permissão da Câmera Consulta Negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            setBitmap()
        }
    }

    private fun setBitmap() {
        val bitmapConsulta = BitmapFactory.decodeFile(arquivoFoto?.absolutePath)
        bitmapConsulta?.let {
            val bitmapConsultaReduzido = resizeBitmap(it, maxWidthHeight, maxWidthHeight)
            imgFoto.setImageBitmap(bitmapConsultaReduzido)
            imgFoto.scaleType = ImageView.ScaleType.FIT_CENTER
            imgFoto.setTag(arquivoFoto?.absolutePath)
        }
    }

    private fun resizeBitmap(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var img = image
        if (maxHeight > 0 && maxWidth > 0) {
            val width = img.width
            val height = img.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            img = Bitmap.createScaledBitmap(img, finalWidth, finalHeight, true)
            return img
        } else {
            return img
        }
    }
}
