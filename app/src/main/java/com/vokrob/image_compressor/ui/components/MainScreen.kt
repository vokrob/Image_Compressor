package com.vokrob.image_compressor.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vokrob.image_compressor.utils.compressImage
import com.vokrob.image_compressor.utils.getFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class CompressionMode { AUTO, MANUAL }

@Composable
fun MainScreen() {
    var bitmapResult by remember { mutableStateOf<Pair<Uri?, Long>?>(null) }
    var compressionMode by remember { mutableStateOf(CompressionMode.AUTO) }
    var targetSizeKB by remember { mutableStateOf("") }

    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val fileSize = getFileSize(context, it)
                bitmapResult = Pair(it, fileSize)
            }
        }
    val snackBarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Image Compressor",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RadioButton(
                    selected = compressionMode == CompressionMode.AUTO,
                    onClick = {
                        compressionMode = CompressionMode.AUTO
                        targetSizeKB = ""
                    }
                )
                Text("Автоматическое")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RadioButton(
                    selected = compressionMode == CompressionMode.MANUAL,
                    onClick = { compressionMode = CompressionMode.MANUAL }
                )
                Text("Ручное")
            }
        }

        if (compressionMode == CompressionMode.MANUAL) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = targetSizeKB,
                onValueChange = { if (it.all { char -> char.isDigit() }) targetSizeKB = it },
                label = { Text(text = "Желаемый размер (КБ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { launcher.launch("image/*") },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Выбрать изображение")
        }

        Spacer(modifier = Modifier.height(16.dp))

        bitmapResult?.let { (uri, fileSize) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val fileSizeInKB = fileSize.toFloat() / 1024
                    if (fileSizeInKB > 1000) {
                        val fileSizeInMB = fileSize.toFloat() / (1024 * 1024)
                        Text("Размер изображения: ${"%.2f".format(fileSizeInMB)} МБ")
                    } else {
                        Text("Размер изображения: ${"%.0f".format(fileSizeInKB)} КБ")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                uri?.let {
                                    val targetSize =
                                        if (compressionMode == CompressionMode.MANUAL) {
                                            targetSizeKB.toIntOrNull()
                                        } else null
                                    compressImage(context, it, compressionMode, targetSize)
                                    snackBarHostState.showSnackbar("Изображение сохранено")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                            text = "Сжать изображение и сохранить",
                            color = Color.White
                        )
                    }
                }
            }
        }
        SnackbarHost(hostState = snackBarHostState)
    }
}





















