package utils
import java.io.File
import java.io.IOException

object SecretLoader {
    fun load(secretPath: String): String {
        val file = File(secretPath)
        if (!file.exists()) {
            throw IOException("Secret file not found: $secretPath")
        }
        return file.readText().trim()
    }
}