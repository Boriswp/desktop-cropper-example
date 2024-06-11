import Utils.AwtDirectoryChooser
import Utils.AwtFileChooser
import Utils.FileSaveDialog
import Utils.Utils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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


sealed class CurrentState {
    data object MainScreen : CurrentState()

    data class DirectoryCropScreen(val listFiles: List<File>) : CurrentState()

    data class FileCropScreen(val filePath: String) : CurrentState()

    data class PreviewScreen(val image: ImageBitmap, val filePath: String) : CurrentState()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            val currentState = remember { mutableStateOf<CurrentState>(CurrentState.MainScreen) }

            val text by remember { mutableStateOf("Choose file") }
            val text1 by remember { mutableStateOf("Choose directory") }
            var isFileChooserOpen by remember { mutableStateOf(false) }
            var isDirectoryChooserOpen by remember { mutableStateOf(false) }


            val scope = rememberCoroutineScope()
            val imageCropper = rememberImageCropper()


            if (isDirectoryChooserOpen) {
                AwtDirectoryChooser {
                    isDirectoryChooserOpen = false
                    currentState.value =
                        CurrentState.DirectoryCropScreen(Utils.getFilesFromPath(it))
                }
            }

            if (isFileChooserOpen) {
                AwtFileChooser(onCloseRequest = {
                    isFileChooserOpen = false
                    currentState.value = CurrentState.FileCropScreen(it)
                    if (it.isNotEmpty()) {
                        scope.launch {
                            try {
                                val result = imageCropper.crop(
                                    bmp = ImageIO.read(File(it)).toComposeImageBitmap()
                                )
                                when (result) {
                                    is CropResult.Cancelled, is CropError -> {
                                        currentState.value = CurrentState.MainScreen
                                    }

                                    is CropResult.Success -> {
                                        currentState.value =
                                            CurrentState.PreviewScreen(result.bitmap, it)
                                    }
                                }
                            } catch (_: IIOException) {
                                currentState.value = CurrentState.MainScreen
                            }
                        }
                    }
                })
            }

            when (currentState.value) {
                CurrentState.MainScreen -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(45.dp)) {
                        Button(onClick = {
                            isFileChooserOpen = true
                        }) {
                            Text(text)
                        }

                        Button(onClick = {
                            isDirectoryChooserOpen = true
                        }) {
                            Text(text1)
                        }
                    }
                }

                is CurrentState.FileCropScreen -> {
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
                    } ?: { currentState.value = CurrentState.MainScreen }
                }

                is CurrentState.PreviewScreen -> {
                    val preview by remember {
                        mutableStateOf((currentState.value as CurrentState.PreviewScreen))
                    }
                    val isFileSaveOpen = remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Image(
                            bitmap = preview.image,
                            contentDescription = null,
                            modifier = Modifier.weight(0.9f)
                        )

                        Button(
                            onClick = {
                                isFileSaveOpen.value = true
                            }, modifier = Modifier.weight(0.15f).padding(16.dp)
                        ) {
                            Text("Save")
                        }
                    }

                    if (isFileSaveOpen.value) {
                        FileSaveDialog(file = preview.filePath.substringAfterLast("/"),
                            onCloseRequest = {
                                isFileSaveOpen.value = false
                                if (it.isNotEmpty()) {
                                    scope.launch {
                                        Utils.saveImageBitmap(
                                            preview.image, it
                                        )
                                        currentState.value = CurrentState.MainScreen
                                    }
                                }
                            })
                    }
                }

                is CurrentState.DirectoryCropScreen -> {
                    val listFiles by remember {
                        mutableStateOf((currentState.value as CurrentState.DirectoryCropScreen).listFiles)
                    }
                    val state = rememberPagerState {
                        listFiles.size
                    }
                    HorizontalPager(state, modifier = Modifier.matchParentSize()) {
                        LaunchedEffect(state.currentPage) {
                            scope.launch {
                                if (!listFiles[it].isFile) {
                                    state.scrollToPage(state.currentPage + 1)
                                }
                                try {
                                    val result = imageCropper.crop(
                                        bmp = ImageIO.read(File(listFiles[it].absolutePath))
                                            .toComposeImageBitmap()
                                    )
                                    when (result) {
                                        is CropResult.Cancelled, is CropError -> {
                                            currentState.value = CurrentState.MainScreen
                                        }

                                        is CropResult.Success -> {
                                            Utils.saveImageBitmap(
                                                result.bitmap, listFiles[it].absolutePath
                                            )
                                            if (state.currentPage + 1 >= state.pageCount) {
                                                currentState.value = CurrentState.MainScreen
                                            } else {
                                                state.scrollToPage(state.currentPage + 1)
                                            }
                                        }
                                    }
                                } catch (_: IIOException) {
                                    currentState.value = CurrentState.MainScreen
                                }
                            }
                        }

                        imageCropper.cropState?.let {
                            ImageCropperDialog(
                                state = it,
                                currentNum = state.currentPage,
                                totalCount = listFiles.size,
                                dialogPadding = PaddingValues(0.dp),
                                dialogShape = RoundedCornerShape(0.dp),
                                style = CropperStyle(
                                    overlay = Color.Gray.copy(alpha = .5f),
                                    autoZoom = false,
                                    guidelines = null,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}




