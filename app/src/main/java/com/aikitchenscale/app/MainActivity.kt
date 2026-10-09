package com.aikitchenscale.app

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val ANALYZE_URL =
    "https://al-kitchen-scale-production-8840.up.railway.app/analyze"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                KitchenScreen()
            }
        }
    }
}

@Composable
fun KitchenScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageBytes by remember {
        mutableStateOf<ByteArray?>(null)
    }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                imageBytes = context.contentResolver
                    .openInputStream(uri)?.use {
                        it.readBytes()
                    }
                result = ""
                error = ""
            } catch (e: Exception) {
                error = e.message ?: "Image error"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AI Kitchen Scale",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Estimate food weight and nutrition from a photo.")

        Button(
            onClick = { picker.launch("image/*") },
            enabled = !loading
        ) {
            Text("Select food photo")
        }

        imageBytes?.let { bytes ->
            val bitmap = remember(bytes) {
                BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.size
                )
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Food photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
        }

        Button(
            onClick = {
                val bytes = imageBytes
                if (bytes != null) {
                    loading = true
                    error = ""
                    result = ""

                    scope.launch {
                        try {
                            result = analyzeFood(bytes)
                        } catch (e: Exception) {
                            error = e.message
                                ?: "Analysis failed"
                        } finally {
                            loading = false
                        }
                    }
                }
            },
            enabled = imageBytes != null && !loading
        ) {
            Text("Analyze food")
        }

        if (loading) {
            CircularProgressIndicator()
            Text("Analyzing...")
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (result.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

private suspend fun analyzeFood(
    image: ByteArray
): String = withContext(Dispatchers.IO) {

    val encoded = Base64.encodeToString(
        image, Base64.NO_WRAP
    )

    val payload = JSONObject().apply {
        put("imageBase64", encoded)
        put("mimeType", "image/jpeg")
        put("language", "en")
    }

    val connection = (
        URL(ANALYZE_URL).openConnection()
            as HttpURLConnection
    )

    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 30000
        connection.readTimeout = 60000
        connection.doOutput = true
        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        connection.outputStream.use {
            it.write(
                payload.toString()
                    .toByteArray(Charsets.UTF_8)
            )
        }

        val status = connection.responseCode

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val response = stream?.bufferedReader()
            ?.use { it.readText() } ?: ""

        if (status !in 200..299) {
            throw Exception(
                "Server error $status: $response"
            )
        }

        val json = JSONObject(response)
        val data = json.optJSONObject("result") ?: json

        val food = data.optString(
            "foodName",
            data.optString("food", "Unknown food")
        )

        val weight = data.optDouble(
            "estimatedWeightG",
            data.optDouble("weightG", 0.0)
        )

        val calories = data.optDouble(
            "calories", 0.0
        )

        val protein = data.optDouble(
            "proteinG",
            data.optDouble("protein", 0.0)
        )

        val carbs = data.optDouble(
            "carbsG",
            data.optDouble("carbs", 0.0)
        )

        val fat = data.optDouble(
            "fatG",
            data.optDouble("fat", 0.0)
        )

        val confidence = data.optString(
            "confidence", "Not available"
        )

        """
        Food: $food

        Estimated weight: $weight g
        Calories: $calories kcal
        Protein: $protein g
        Carbohydrates: $carbs g
        Fat: $fat g

        Confidence: $confidence
        """.trimIndent()

    } finally {
        connection.disconnect()
    }
}
