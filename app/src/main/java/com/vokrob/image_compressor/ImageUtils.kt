package com.vokrob.image_compressor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

fun compressImage(context: Context, uri: Uri) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val compressedFile = File(context.cacheDir, "compressed_image.jpg")
    val outputStream = FileOutputStream(compressedFile)

    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
    outputStream.flush()
    outputStream.close()

    saveImage(context, compressedFile)
}

private fun saveImage(context: Context, imageFile: File) {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name) // Имя файла
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // MIME-тип
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            Environment.DIRECTORY_PICTURES
        )
    }

    val uri: Uri? =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    if (uri != null) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            imageFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}




















