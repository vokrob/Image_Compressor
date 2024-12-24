package com.vokrob.image_compressor.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.vokrob.image_compressor.ui.components.CompressionMode
import java.io.File
import java.io.FileOutputStream

fun getFileSize(context: Context, uri: Uri): Long {
    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return 0
    return fileDescriptor.statSize
}

fun compressImage(
    context: Context,
    uri: Uri,
    compressionMode: CompressionMode,
    targetSizeKB: Int? = null
): Uri {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val compressedFile = File(context.cacheDir, "compressed_image.jpg")

    when (compressionMode) {
        CompressionMode.AUTO -> {
            FileOutputStream(compressedFile).use { outputStream ->
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            }
        }

        CompressionMode.MANUAL -> {
            targetSizeKB?.let { targetSize ->
                var quality = 100
                var fileSize: Long

                do {
                    FileOutputStream(compressedFile).use { outputStream ->
                        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    }
                    fileSize = compressedFile.length() / 1024
                    quality -= 5
                } while (fileSize > targetSize && quality > 0)
            }
        }
    }
    return Uri.fromFile(compressedFile)
}

fun saveImage(context: Context, uri: Uri) {
    val values = ContentValues().apply {
        put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "compressed_image_${System.currentTimeMillis()}.jpg"
        )
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    val saveUri =
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    saveUri?.let { destinationUri ->
        context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}




















