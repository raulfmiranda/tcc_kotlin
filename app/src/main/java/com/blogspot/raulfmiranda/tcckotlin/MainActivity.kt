package com.blogspot.raulfmiranda.tcckotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix

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

        btnApplyFilter.setOnClickListener {
            applyFilter()
        }
    }

    private fun uploadFoto() {
        arquivoFoto?.let {
            val storageReference = FirebaseStorage.getInstance().getReference().child(it.name)
            val uploadTask = storageReference.putFile(Uri.fromFile(it))

            uploadTask.addOnFailureListener {
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                Log.e("firebaseerror", it.message)
            }.addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Foto enviada com sucesso.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun geraCaminhoFoto(): String? {
        val caminho = getExternalFilesDir(null)?.path + "/" + System.currentTimeMillis() + ".jpg"
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
        val bitmap = BitmapFactory.decodeFile(arquivoFoto?.absolutePath)
        bitmap?.let {
            val bitmapConsultaReduzido = resizeBitmap(it, maxWidthHeight, maxWidthHeight)
            imgFoto.setImageBitmap(bitmapConsultaReduzido)
            imgFoto.scaleType = ImageView.ScaleType.FIT_CENTER
            imgFoto.setTag(arquivoFoto?.absolutePath)
        }
    }

    private fun applyFilter() {
        val bitmap = BitmapFactory.decodeFile(arquivoFoto?.absolutePath)
        bitmap?.let {
            val bmp = resizeBitmap(it, maxWidthHeight, maxWidthHeight)
            val bitmapGray = doGrayScale(bmp)
            imgFoto.setImageBitmap(bitmapGray)
            imgFoto.scaleType = ImageView.ScaleType.FIT_CENTER
            imgFoto.setTag(arquivoFoto?.absolutePath)
            bitmapToFile(bitmapGray)
        }
    }

    private fun doGrayScale(bmpOriginal: Bitmap): Bitmap {
        val width: Int
        val height: Int
        height = bmpOriginal.height
        width = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixFilter
        canvas.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun bitmapToFile(bmp: Bitmap) {
        val bos = ByteArrayOutputStream()
        bmp.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
        val bitmapdata = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(arquivoFoto)
        fos.write(bitmapdata)
        fos.flush()
        fos.close()
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
