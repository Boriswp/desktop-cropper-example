package Utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toBufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.imageio.ImageIO


object Utils {

    suspend fun saveImageBitmap(imageBitmap: ImageBitmap, filePath: String) {
        withContext(Dispatchers.IO) {
            try {
                val bufferedImage = imageBitmap.asSkiaBitmap().toBufferedImage()
                var totalPath = filePath
                var format = totalPath.substringAfterLast(".", missingDelimiterValue = "")
                if (format.isEmpty()) {
                    format = "png"
                    totalPath = "$filePath.$format"
                }

                val file = File(totalPath)
                if (file.exists()) {
                    println("File already exists. It will be overwritten.")
                }

                val success = ImageIO.write(
                    bufferedImage,
                    format,
                    file
                )

                if (!success) {
                    println("Failed to write image.")
                }

            } catch (e: Exception) {
                println("An error occurred while saving the image: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    fun getFilesFromPath(directory: String): List<File> {
        return Files.walk(Paths.get(directory)).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        }
    }
}