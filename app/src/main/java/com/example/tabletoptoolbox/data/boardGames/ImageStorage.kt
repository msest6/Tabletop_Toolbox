package com.example.tabletoptoolbox.data.boardGames

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.util.UUID

object ImageStorage {

    fun createCameraCaptureUri(context: Context): Uri {
        val tempDir = File(context.cacheDir, "camera_temp").apply { mkdirs() }
        val tempFile = File(tempDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Kompresira odabranu/snimljenu sliku i sprema je u CACHE (privremeni prostor),
     * ne u trajni storage. Ovo se zove čim korisnik odabere sliku, prije spremanja igre.
     * Cache direktorij Android sam čisti po potrebi, pa i ako korisnik nikad ne
     * pozove finalizeImage/deleteImage, fajl neće trajno zauzimati prostor.
     */
    fun compressToTempFile(context: Context, sourceUri: Uri, maxWidth: Int = 800): String {
        val tempDir = File(context.cacheDir, "temp_images").apply { mkdirs() }
        val tempFile = File(tempDir, "temp_${UUID.randomUUID()}.jpg")

        val originalBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: throw IllegalStateException("Slika se nije mogla učitati")

        val rotatedBitmap = fixOrientation(context, sourceUri, originalBitmap)

        val targetWidth = if (rotatedBitmap.width > maxWidth) maxWidth else rotatedBitmap.width
        val targetHeight = (rotatedBitmap.height * (targetWidth.toFloat() / rotatedBitmap.width)).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)

        tempFile.outputStream().use { output ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        }

        if (rotatedBitmap != originalBitmap) rotatedBitmap.recycle()
        originalBitmap.recycle()
        resizedBitmap.recycle()

        return tempFile.absolutePath
    }

    /**
     * Premješta privremenu (cache) sliku u trajni storage aplikacije.
     * Zove se ISKLJUČIVO pri uspješnom spremanju igre u bazu.
     * Vraća apsolutnu putanju do trajnog fajla.
     */
    fun finalizeImage(context: Context, tempPath: String): String {
        val tempFile = File(tempPath)
        val destDir = File(context.filesDir, "game_images").apply { mkdirs() }
        val destFile = File(destDir, "game_${UUID.randomUUID()}.jpg")

        tempFile.copyTo(destFile, overwrite = true)
        tempFile.delete() // temp više ne treba

        return destFile.absolutePath
    }

    /** Briše fajl slike s diska - koristi se za brisanje temp slike ako korisnik odustane. */
    fun deleteImage(path: String) {
        File(path).takeIf { it.exists() }?.delete()
    }

    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        val exifStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val exif = ExifInterface(exifStream)
        exifStream.close()

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }

        val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}