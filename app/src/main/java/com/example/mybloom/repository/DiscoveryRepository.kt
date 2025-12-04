package com.example.mybloom.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.mybloom.dao.DiscoveryDao
import com.example.mybloom.entities.DiscoveryEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class DiscoveryRepository(
    private val discoveryDao: DiscoveryDao,
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()

    // Clé API Pl@ntNet
    private val plantNetKey = "2b10r2jeB0pBpDL4dzks666" //clé
    private val plantNetUrl = "https://my-api.plantnet.org/v2/identify/all"

    // Requêtes
    private val client = OkHttpClient()

    // Récupérer les découvertes de l'utilisateur connecté
    fun getUserDiscoveries(): Flow<List<DiscoveryEntity>> {
        val userId = auth.currentUser?.uid ?: ""
        return discoveryDao.getUserDiscoveries(userId)
    }

    // Identifier une plante avec Pl@ntNet + Wiki et sauvegarder dans Room
    suspend fun identifyAndSavePlant(bitmap: Bitmap): Result<DiscoveryEntity> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // 1. Sauvegarder temporairement l'image
            val tempFile = File(context.cacheDir, "plant_tmp.jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            // 2. Requête multipart Pl@ntNet
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("images", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .addFormDataPart("organs", "auto")
                .build()
            val request = Request.Builder()
                .url("$plantNetUrl?api-key=$plantNetKey")
                .post(requestBody)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("No response from Pl@ntNet API")

            // 3. Parsing avec gestion d'erreur "plante uniquement"
            val (plantName, wikiDesc, isPlant) = parsePlantNetAndWikiWithError(responseBody)

            if (!isPlant) {
                return@withContext Result.failure(Exception("Seules les plantes peuvent être identifiées par cette application. Merci de soumettre une photo de plante."))
            }

            // 4. Sauvegarder l'image localement
            val localImagePath = saveImageLocally(bitmap)

            // 5. Créer l'entité DiscoveryEntity
            val discovery = DiscoveryEntity(
                userId = userId,
                plantName = plantName,
                aiFact = wikiDesc,
                localImagePath = localImagePath,
                timestamp = System.currentTimeMillis()
            )
            val insertedId = discoveryDao.insertDiscovery(discovery)
            Result.success(discovery.copy(id = insertedId.toInt()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Nouvelle fonction de parsing avec détection du cas "pas une plante"
    private fun parsePlantNetAndWikiWithError(json: String): Triple<String, String, Boolean> {
        val obj = JSONObject(json)
        val results = obj.optJSONArray("results")
        // Cas d'absence totale de résultat
        if (results == null || results.length() == 0) {
            return Triple("Inconnu", "Aucune plante détectée.", false)
        }
        // Recherche du premier nom scientifique plausible
        for (i in 0 until results.length()) {
            val speciesObj = results.optJSONObject(i)?.optJSONObject("species") ?: continue
            val scientificNameRaw = speciesObj.optString("scientificNameWithoutAuthor", "")
            val scientificName = scientificNameRaw.trim()
                .replace("\\s+".toRegex(), " ")
                .replace("’", "'")
            // Si le nom est inexploitable ou type "spp." on ignore
            if (scientificName.isEmpty() || scientificName.contains("spp", ignoreCase = true)) continue
            println("NOM SCIENTIFIQUE UTILISÉ : $scientificName")
            val commonNames = buildList {
                val arr = speciesObj.optJSONArray("commonNames")
                if (arr != null) for (j in 0 until arr.length()) add(arr.optString(j).trim())
            }
            val wikiDesc = fetchWikipediaDescription(scientificName, commonNames)
            if (wikiDesc != "No wikipedia description found.") {
                return Triple(scientificName, wikiDesc, true)
            }
        }
        // Aucun résultat exploitable —> pas une plante
        return Triple("Inconnu", "Seules les plantes peuvent être identifiées par cette application.", false)
    }

    private fun fetchWikipediaDescription(scientificName: String, commonNames: List<String>? = null): String {
        val namesToTry = mutableListOf(
            scientificName.replace(" ", "_"),
            scientificName
        )
        commonNames?.filter { it.isNotBlank() }?.forEach {
            namesToTry.add(it.replace(" ", "_"))
            namesToTry.add(it)
        }
        val urls = namesToTry.distinct().flatMap {
            listOf(
                "https://en.wikipedia.org/api/rest_v1/page/summary/$it",
                "https://fr.wikipedia.org/api/rest_v1/page/summary/$it"
            )
        }
        for (url in urls) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "MyBloomApp/1.0 (contact: tonmail@example.com)") // Obligatoire pour Wiki maintenant
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val httpCode = response.code
                val body = response.body?.string() ?: continue
                println("TEST WIKI : $url (HTTP $httpCode)")
                println("RÉPONSE : $body")
                val obj = JSONObject(body)
                val desc = obj.optString("extract", "")
                if (desc.isNotEmpty()
                    && !desc.equals("Other reasons this message may be displayed:", ignoreCase = true)) {
                    return desc
                }
            } catch (e: Exception) {
                println("Erreur sur $url : ${e.message}")
            }
        }
        return "No wikipedia description found."
    }

    // Récupérer une découverte par ID
    suspend fun getDiscoveryById(id: Int): DiscoveryEntity? = withContext(Dispatchers.IO) {
        discoveryDao.getDiscoveryById(id)
    }

    // Supprimer une découverte
    suspend fun deleteDiscovery(discovery: DiscoveryEntity) {
        withContext(Dispatchers.IO) {
            val imageFile = File(discovery.localImagePath)
            if (imageFile.exists()) imageFile.delete()
            discoveryDao.deleteDiscovery(discovery)
        }
    }

    // Rechercher des découvertes
    fun searchDiscoveries(query: String): Flow<List<DiscoveryEntity>> {
        val userId = auth.currentUser?.uid ?: ""
        return discoveryDao.searchDiscoveriesByName(userId, query)
    }

    // Obtenir le nombre de découvertes
    suspend fun getDiscoveryCount(): Int = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: ""
        discoveryDao.getUserDiscoveryCount(userId)
    }

    // Sauvegarde image locale
    private suspend fun saveImageLocally(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val filename = "plant_${System.currentTimeMillis()}.jpg"
        val directory = File(context.filesDir, "plant_images")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, filename)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.absolutePath
    }
}
