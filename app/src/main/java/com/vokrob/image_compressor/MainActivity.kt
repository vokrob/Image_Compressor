package com.vokrob.image_compressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vokrob.image_compressor.ui.theme.Image_CompressorTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Image_CompressorTheme {
                MainContent()
            }
        }
    }
}

@Preview(showBackground = true)

@Composable
fun MainContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val bitmapResult = remember { mutableStateOf<Uri?>(null) }
        val context = LocalContext.current
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                bitmapResult.value = uri
            }

        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Выбрать изображение")
        }
        Spacer(modifier = Modifier.height(16.dp))

        bitmapResult.value?.let { uri ->
            AsyncImage(model = uri.toString(), contentDescription = null)
            Button(onClick = { compressImage(context, uri) }) {
                Text(text = "Сжать изображение")
            }
        }
    }
}

private fun compressImage(context: Context, uri: Uri) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val compressedFile = File(context.cacheDir, "compressed_image.jpg")
    val outputStream = FileOutputStream(compressedFile)

    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
    outputStream.flush()
    outputStream.close()
}




















