package Utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toBufferedImage
import java.io.File
import javax.imageio.ImageIO

object Utils {

    suspend fun saveImageBitmap(imageBitmap: ImageBitmap, filePath: String) {
        withContext(Dispatchers.IO) {
            val bufferedImage = imageBitmap.asSkiaBitmap().toBufferedImage()
            var totalPath = filePath
            var format = totalPath.substringAfterLast(".", missingDelimiterValue = "")
            if (format.isEmpty()) {
                format = ".png"
                totalPath = filePath + format
            }

            ImageIO.write(
                bufferedImage,
                format,
                File(totalPath)
            )
        }
    }

}