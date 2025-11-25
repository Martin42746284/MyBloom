package com.example.mybloom.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.mybloom.dao.DiscoveryDao
import com.example.mybloom.entities.DiscoveryEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class DiscoveryRepository(
    private val discoveryDao: DiscoveryDao,
    private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()

    // Initialiser le modèle Gemini via Firebase Vertex AI
    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    // Récupérer les découvertes de l'utilisateur connecté
    fun getUserDiscoveries(): Flow<List<DiscoveryEntity>> {
        val userId = auth.currentUser?.uid ?: ""
        return discoveryDao.getUserDiscoveries(userId)
    }

    // Identifier une plante avec Gemini et sauvegarder dans Room
    suspend fun identifyAndSavePlant(bitmap: Bitmap): Result<DiscoveryEntity> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext Result.failure(Exception("User not authenticated"))

            // 1. Appel à Gemini pour identifier la plante
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("""
                        Identify this plant, flower, tree, or botanical specimen.
                        
                        Respond in the following format:
                        
                        Line 1: The formal name (scientific or common name)
                        Lines 2-3: An interesting fact or description in two sentences maximum.
                        
                        Example:
                        Monstera deliciosa
                        This tropical plant is known for its large, perforated leaves that develop naturally as it matures. Native to Central American rainforests, it's also called the "Swiss cheese plant" due to its distinctive holes.
                    """.trimIndent())
                }
            )

            val responseText = response.text ?: throw Exception("No response from Gemini AI")

            // 2. Parser la réponse
            val (plantName, aiFact) = parseGeminiResponse(responseText)

            // 3. Sauvegarder l'image localement
            val localImagePath = saveImageLocally(bitmap)

            // 4. Créer l'entité DiscoveryEntity avec les nouveaux noms de champs
            val discovery = DiscoveryEntity(
                userId = userId,
                plantName = plantName,           // ⬅️ Nouveau nom
                aiFact = aiFact,                 // ⬅️ Nouveau nom
                localImagePath = localImagePath, // ⬅️ Nouveau nom
                timestamp = System.currentTimeMillis()
            )

            // 5. Insérer dans Room
            val insertedId = discoveryDao.insertDiscovery(discovery)

            Result.success(discovery.copy(id = insertedId.toInt()))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Récupérer une découverte par ID
    suspend fun getDiscoveryById(id: Int): DiscoveryEntity? {
        return withContext(Dispatchers.IO) {
            discoveryDao.getDiscoveryById(id)
        }
    }

    // Supprimer une découverte
    suspend fun deleteDiscovery(discovery: DiscoveryEntity) {
        withContext(Dispatchers.IO) {
            // Supprimer l'image du stockage local
            val imageFile = File(discovery.localImagePath)
            if (imageFile.exists()) {
                imageFile.delete()
            }

            // Supprimer de Room
            discoveryDao.deleteDiscovery(discovery)
        }
    }

    // Rechercher des découvertes
    fun searchDiscoveries(query: String): Flow<List<DiscoveryEntity>> {
        val userId = auth.currentUser?.uid ?: ""
        return discoveryDao.searchDiscoveriesByName(userId, query)
    }

    // Obtenir le nombre de découvertes
    suspend fun getDiscoveryCount(): Int {
        val userId = auth.currentUser?.uid ?: ""
        return withContext(Dispatchers.IO) {
            discoveryDao.getUserDiscoveryCount(userId)
        }
    }

    // Parser la réponse de Gemini
    private fun parseGeminiResponse(text: String): Pair<String, String> {
        val lines = text.trim().lines().filter { it.isNotBlank() }

        val plantName = lines.firstOrNull()?.trim() ?: "Unknown Plant"
        val aiFact = lines.drop(1).joinToString(" ").trim().ifEmpty {
            "No additional information available."
        }

        return Pair(plantName, aiFact)
    }

    // Sauvegarder l'image en local
    private suspend fun saveImageLocally(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val filename = "plant_${System.currentTimeMillis()}.jpg"
        val directory = File(context.filesDir, "plant_images")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        file.absolutePath
    }
}

