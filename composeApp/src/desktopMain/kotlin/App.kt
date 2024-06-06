
import Utils.AwtFileChooser
import Utils.FileSaveDialog
import Utils.Utils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import easycrop.CropperStyle
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.ImageCropperDialog
import java.io.File
import javax.imageio.IIOException
import javax.imageio.ImageIO


@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val text by remember { mutableStateOf("Choose file") }
            var isFileChooserOpen by remember { mutableStateOf(false) }
            val isFileSaveOpen = remember { mutableStateOf(false) }
            val croppedImage = remember { mutableStateOf<ImageBitmap?>(null) }
            val filePath = remember {
                mutableStateOf("")
            }

            val scope = rememberCoroutineScope()
            val imageCropper = rememberImageCropper()

            if (isFileSaveOpen.value) {
                FileSaveDialog(
                    file = filePath.value.substringAfterLast("/"),
                    onCloseRequest = {
                        isFileSaveOpen.value = false
                        if (it.isNotEmpty() && croppedImage.value != null) {
                            scope.launch {
                                Utils.saveImageBitmap(croppedImage.value!!,it)
                                croppedImage.value = null
                                filePath.value = ""
                            }
                        }
                    }
                )
            }

            if (isFileChooserOpen) {
                AwtFileChooser(
                    onCloseRequest = {
                        isFileChooserOpen = false
                        print(it)
                        filePath.value = it
                        if (filePath.value.isNotEmpty()) {
                            scope.launch {
                                try {
                                    val result = imageCropper.crop(
                                        bmp = ImageIO.read(File(filePath.value))
                                            .toComposeImageBitmap()
                                    )
                                    when (result) {
                                        is CropResult.Cancelled -> {
                                            filePath.value = ""
                                        }

                                        is CropError -> {
                                            filePath.value = ""
                                        }

                                        is CropResult.Success -> {
                                            croppedImage.value = result.bitmap
                                        }
                                    }
                                }catch (_: IIOException){
                                    filePath.value = ""
                                }
                            }
                        }
                    }
                )
            }

            if (filePath.value.isNotEmpty()) {
                imageCropper.cropState?.let {
                    ImageCropperDialog(
                        state = it,
                        dialogPadding = PaddingValues(0.dp),
                        dialogShape = RoundedCornerShape(0.dp),
                        style = CropperStyle(
                            overlay = Color.Gray.copy(alpha = .5f),
                            autoZoom = false,
                            guidelines = null,
                        )
                    )
                }
            } else {
                Button(onClick = {
                    isFileChooserOpen = true
                }) {
                    Text(text)
                }
            }
            croppedImage.value?.let {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        modifier = Modifier.weight(0.9f)
                    )

                    Button(
                        onClick = {
                            isFileSaveOpen.value = true
                        },
                        modifier = Modifier.weight(0.15f)
                            .padding(16.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}




