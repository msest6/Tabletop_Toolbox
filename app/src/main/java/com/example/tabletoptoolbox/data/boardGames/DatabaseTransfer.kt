package com.example.tabletoptoolbox.data.boardGames

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Izvoz/uvoz kolekcije igara kao ZIP arhive (.ttbx).
 * Arhiva sadrži:
 *   - games.json  -> atributi svih igara (BEZ elo-a i comparisonCount-a)
 *   - images/     -> stvarni fajlovi slika, jer imagePath ne vrijedi na drugom uređaju
 *
 * Pri uvozu se igre spremaju kroz repository.addGame(...), koji im automatski
 * postavlja eloRating = 1200 i comparisonCount = 0. Nema kviza/evaluacije.
 */
object DatabaseTransfer {

    private const val MANIFEST_NAME = "games.json"
    private const val IMAGES_DIR = "images"
    private const val FORMAT_VERSION = 1

    sealed interface ImportResult {
        data class Success(val importedCount: Int) : ImportResult
        data class Error(val message: String) : ImportResult
    }

    // ============================================================
    //  IZVOZ
    // ============================================================

    /** Zapakira sve igre u .ttbx fajl u cacheu i vrati FileProvider Uri spreman za dijeljenje. */
    suspend fun exportToCache(
        context: Context,
        games: List<GameEntity>
    ): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "db_export").apply { mkdirs() }
        exportDir.listFiles()?.forEach { it.delete() } // makni stare izvoze

        val exportFile = File(exportDir, "tabletier_kolekcija_${System.currentTimeMillis()}.ttbx")

        ZipOutputStream(BufferedOutputStream(exportFile.outputStream())).use { zip ->
            val gamesArray = JSONArray()

            games.forEachIndexed { index, game ->
                val obj = JSONObject().apply {
                    put("name", game.name)
                    put("description", game.description)
                    put("userRating", game.userRating.toDouble())
                    put("playTimeMinutes", game.playTimeMinutes)
                    put("minPlayers", game.minPlayers)
                    put("maxPlayers", game.maxPlayers ?: JSONObject.NULL)
                }

                val imageFile = game.imagePath?.let { File(it) }?.takeIf { it.exists() }
                if (imageFile != null) {
                    val entryName = "$IMAGES_DIR/img_$index.jpg"
                    obj.put("image", entryName)
                    zip.putNextEntry(ZipEntry(entryName))
                    imageFile.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                } else {
                    obj.put("image", JSONObject.NULL)
                }

                gamesArray.put(obj)
            }

            val root = JSONObject().apply {
                put("version", FORMAT_VERSION)
                put("games", gamesArray)
            }

            zip.putNextEntry(ZipEntry(MANIFEST_NAME))
            zip.write(root.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
    }

    // ============================================================
    //  UVOZ
    // ============================================================

    /**
     * Učita .ttbx s dane Uri, doda sve igre u bazu (elo 1200, bez evaluacije).
     * Ne provjerava duplikate - ponovni uvoz dodaje igre iznova.
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        repository: GameRepository
    ): ImportResult = withContext(Dispatchers.IO) {
        // Kopiraj u temp fajl da bismo mogli koristiti ZipFile (nasumičan pristup entryjima).
        val tempZip = File(context.cacheDir, "db_import_${System.currentTimeMillis()}.zip")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempZip.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext ImportResult.Error("Datoteka se nije mogla otvoriti.")

            ZipFile(tempZip).use { zipFile ->
                val manifestEntry = zipFile.getEntry(MANIFEST_NAME)
                    ?: return@withContext ImportResult.Error("Neispravna datoteka: nedostaje popis igara.")

                val jsonText = zipFile.getInputStream(manifestEntry).use {
                    it.readBytes().toString(Charsets.UTF_8)
                }

                val root = JSONObject(jsonText)
                val gamesArray = root.getJSONArray("games")
                val destDir = File(context.filesDir, "game_images").apply { mkdirs() }

                var imported = 0
                for (i in 0 until gamesArray.length()) {
                    val obj = gamesArray.getJSONObject(i)

                    val name = obj.optString("name").trim()
                    if (name.isEmpty()) continue // preskoči neispravan zapis

                    val description = obj.optString("description", "")
                    val userRating = obj.optDouble("userRating", 0.0).toFloat()
                    val playTimeMinutes = obj.optInt("playTimeMinutes", 0)
                    val minPlayers = obj.optInt("minPlayers", 1)
                    val maxPlayers = if (obj.isNull("maxPlayers")) null else obj.optString("maxPlayers")

                    val imagePath = extractImage(zipFile, obj, destDir)

                    repository.addGame(
                        name = name,
                        description = description,
                        imagePath = imagePath,   // addGame postavlja elo=1200, comparisonCount=0
                        userRating = userRating,
                        playTimeMinutes = playTimeMinutes,
                        minPlayers = minPlayers,
                        maxPlayers = maxPlayers
                    )
                    imported++
                }

                ImportResult.Success(imported)
            }
        } catch (e: Exception) {
            ImportResult.Error("Uvoz nije uspio: ${e.message}")
        } finally {
            tempZip.delete()
        }
    }

    /** Raspakira sliku igre (ako postoji) u trajni storage i vrati apsolutnu putanju. */
    private fun extractImage(zipFile: ZipFile, obj: JSONObject, destDir: File): String? {
        if (obj.isNull("image")) return null
        val entryName = obj.optString("image").takeIf { it.isNotEmpty() } ?: return null
        val entry = zipFile.getEntry(entryName) ?: return null

        val destFile = File(destDir, "game_${UUID.randomUUID()}.jpg")
        zipFile.getInputStream(entry).use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        }
        return destFile.absolutePath
    }
}