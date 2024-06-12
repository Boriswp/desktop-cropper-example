package ui


import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cropperproject.easycrop.generated.resources.restore
import cropperproject.easycrop.generated.resources.Res
import easycrop.AspectRatio
import easycrop.CropState
import easycrop.CropperStyle
import easycrop.DefaultCropperStyle
import easycrop.LocalCropperStyle
import easycrop.utils.setAspect
import org.jetbrains.compose.resources.painterResource

private val CropperDialogProperties = (DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = false
))

@Composable
fun ImageCropperDialog(
    state: CropState,
    currentNum: Int,
    totalCount: Int,
    style: CropperStyle = DefaultCropperStyle,
    dialogProperties: DialogProperties = CropperDialogProperties,
    dialogPadding: PaddingValues = PaddingValues(16.dp),
    dialogShape: Shape = RoundedCornerShape(8.dp),
    topBar: @Composable (CropState) -> Unit = { DefaultTopBar(it) },
    cropControls: @Composable BoxScope.(CropState) -> Unit = { DefaultControls(it, false) }
) {
    CompositionLocalProvider(LocalCropperStyle provides style) {
        Dialog(
            onDismissRequest = { state.done(accept = false) },
            properties = dialogProperties,
        ) {
            val requester = remember { FocusRequester() }

            Surface(
                modifier = Modifier.padding(dialogPadding),
                shape = dialogShape,
            ) {
                Column {
                    topBar(state)
                    Text(
                        "${currentNum}/${totalCount}",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()
                            .focusable()
                            .focusRequester(requester)
                            .onKeyEvent {
                                when (it.key) {
                                    Key.Q -> {
                                        state.region = state.region.setAspect(AspectRatio(3, 4))
                                        true
                                    }

                                    Key.Spacebar -> {
                                        state.done(true)
                                        true
                                    }

                                    else -> {
                                        false
                                    }
                                }
                            }
                    ) {
                        CropperPreview(state = state, modifier = Modifier.fillMaxSize())
                        cropControls(state)

                        LaunchedEffect(Unit) {
                            requester.requestFocus()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ImageCropperDialog(
    state: CropState,
    style: CropperStyle = DefaultCropperStyle,
    dialogProperties: DialogProperties = CropperDialogProperties,
    dialogPadding: PaddingValues = PaddingValues(16.dp),
    dialogShape: Shape = RoundedCornerShape(8.dp),
    topBar: @Composable (CropState) -> Unit = { DefaultTopBar(it) },
    cropControls: @Composable BoxScope.(CropState) -> Unit = { DefaultControls(it, false) }
) {
    CompositionLocalProvider(LocalCropperStyle provides style) {
        Dialog(
            onDismissRequest = { state.done(accept = false) },
            properties = dialogProperties,
        ) {

            val requester = remember { FocusRequester() }
            Surface(
                modifier = Modifier.padding(dialogPadding),
                shape = dialogShape,
            ) {
                Column {
                    topBar(state)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clipToBounds()
                            .focusable()
                            .focusRequester(requester)
                            .onKeyEvent {
                                when (it.key) {
                                    Key.Q -> {
                                        state.region = state.region.setAspect(AspectRatio(3, 4))
                                        true
                                    }

                                    Key.Spacebar -> {
                                        state.done(true)
                                        true
                                    }

                                    else -> {
                                        false
                                    }
                                }
                            }
                    ) {
                        CropperPreview(
                            state = state,
                            modifier = Modifier.fillMaxSize()
                        )
                        cropControls(state)

                        LaunchedEffect(Unit) {
                            requester.requestFocus()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.DefaultControls(state: CropState, verticalControls: Boolean) {
    CropperControls(
        isVertical = verticalControls,
        state = state,
        modifier = Modifier
            .align(if (!verticalControls) Alignment.BottomCenter else Alignment.CenterEnd)
            .padding(12.dp),
    )
}

@Composable
fun DefaultTopBar(state: CropState) {
    TopAppBar(title = {},
        navigationIcon = {
            IconButton(onClick = { state.done(accept = false) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        actions = {
            IconButton(onClick = { state.reset() }) {
                Icon(painterResource(Res.drawable.restore), null)
            }
            IconButton(
                onClick = { state.done(accept = true) },
                enabled = !state.accepted,
            ) {
                Icon(Icons.Default.Done, null)
            }
        }
    )
}
