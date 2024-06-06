package Utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun FileDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        val fileDialog = MyFileDialog(parent, "Choose a file", FileDialog.LOAD) {
            onCloseRequest(it)
        }
        fileDialog.filenameFilter =
            FilenameFilter { dir, name ->
                name.endsWith(".jpg") || name.endsWith(".png")
            }
        fileDialog
    },
    dispose = FileDialog::dispose
)


@Composable
fun FileSaveDialog(
    parent: Frame? = null,
    onCloseRequest: (result: String) -> Unit,
    file: String
) = AwtWindow(
    create = {
        val fileDialog = MyFileDialog(parent, "Save a file", FileDialog.SAVE) {
            onCloseRequest(it)
        }
        fileDialog.file = file
        fileDialog
    },
    dispose = FileDialog::dispose
)

@Composable
fun AwtFileChooser(onCloseRequest: (result: String) -> Unit) {
    AwtWindow(create = {
        val frame = JFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val fileChooser = JFileChooser()

        val filter = FileNameExtensionFilter(
            "Images", "png", "jpg"
        )
        fileChooser.fileFilter = filter

        val returnValue = fileChooser.showOpenDialog(frame)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            onCloseRequest(fileChooser.selectedFile.absolutePath)
        } else {
            onCloseRequest("")
        }
        frame
    }, dispose = JFrame::dispose)
}


class MyFileDialog(
    parent: Frame?,
    title: String,
    mode: Int,
    val onCloseRequest: (result: String) -> Unit
) :
    FileDialog(parent, title, mode) {
    override fun setVisible(value: Boolean) {
        super.setVisible(value)
        if (value) {
            if (directory != null || file != null) {
                onCloseRequest("$directory/$file")
            } else {
                onCloseRequest("")
            }
        }
    }
}