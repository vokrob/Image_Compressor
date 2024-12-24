package com.vokrob.image_compressor.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vokrob.image_compressor.utils.compressImage
import com.vokrob.image_compressor.utils.getFileSize
import com.vokrob.image_compressor.utils.saveImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CompressionMode { AUTO, MANUAL }

@Composable
fun MainScreen() {
    var bitmapResult by remember { mutableStateOf<Pair<Uri?, Long>?>(null) }
    var compressedUri by remember { mutableStateOf<Uri?>(null) }
    var compressionMode by remember { mutableStateOf(CompressionMode.AUTO) }
    var targetSizeKB by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSaveButton by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val fileSize = getFileSize(context, it)
                bitmapResult = Pair(it, fileSize)
                compressedUri = null
                showSaveButton = false
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            when {
                bitmapResult == null -> {
                    Text("Выбрать изображение")
                }

                else -> {
                    Box(contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = compressedUri ?: bitmapResult?.first,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        bitmapResult?.let { (_, fileSize) ->
            Spacer(modifier = Modifier.height(8.dp))

            val fileSizeInKB = fileSize.toFloat() / 1024
            if (fileSizeInKB > 1000) {
                val fileSizeInMB = fileSize.toFloat() / (1024 * 1024)
                Text("Размер изображения: ${"%.2f".format(fileSizeInMB)} МБ")
            } else {
                Text("Размер изображения: ${"%.0f".format(fileSizeInKB)} КБ")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(
                    selected = compressionMode == CompressionMode.AUTO,
                    onClick = {
                        compressionMode = CompressionMode.AUTO
                        targetSizeKB = ""
                    }
                )
                Text("Авто")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(
                    selected = compressionMode == CompressionMode.MANUAL,
                    onClick = { compressionMode = CompressionMode.MANUAL }
                )
                Text("Вручную")
            }
        }

        if (compressionMode == CompressionMode.MANUAL) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = targetSizeKB,
                onValueChange = { if (it.all { char -> char.isDigit() }) targetSizeKB = it },
                label = { Text("Введите размер (КБ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.Main) {
                    isLoading = true
                    try {
                        bitmapResult?.first?.let {
                            val targetSize = if (compressionMode == CompressionMode.MANUAL) {
                                targetSizeKB.toIntOrNull()
                            } else null
                            withContext(Dispatchers.IO) {
                                val compressed =
                                    compressImage(context, it, compressionMode, targetSize)
                                withContext(Dispatchers.Main) {
                                    compressedUri = compressed
                                    showSaveButton = true
                                }
                            }
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = bitmapResult != null && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Сжатие..." else "Сжать")
            }
        }

        if (showSaveButton) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        compressedUri?.let { uri ->
                            saveImage(context, uri)
                            snackBarHostState.showSnackbar("Изображение сохранено")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Сохранить")
            }
        }
        SnackbarHost(hostState = snackBarHostState)
    }
}



















