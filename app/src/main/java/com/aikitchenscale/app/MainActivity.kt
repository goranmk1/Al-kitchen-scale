package com.aikitchenscale 


import
android.graphics.Biitmapfactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import
androidx.activity.ComponentActivity
import
androidx.activity.compose.setContent
import
androidx.activity.compose.rememberLa
uncherForActivityResult
import
androidx.activity.result.contract.ActivityResultContracts
import
androidx.compose.foundation.Image
import
androidx.compose.foundation.Iayout.Arrangment
androidx.compose.foundation.Iayout.fillMaxSize
import
androidx.compose.foundation.Iayout.fillMaxWidth
import
androidx.compose.foundation.Iayout.heightIn
import
androidx.compose.foundation.Iayout.padding
import
androidx.compose.foundation.rememberScrollState
import
androidx.compose.foundation.verticalScroll
import
androidx.compose.materia13.Button
import androidx.compose.materia13.Card
import
androidx.compose.material13.CircularProgressIndicator
import
androidx.compose.material13.MateriaITheme
import androidx.compose.material13.Text
import
androidx.compose.runtime.Composable
import
androidx.compose.runtime.getValue
import
androidx.compose.runtime.mutableStateOf
import
androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import
androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL 

data class FoodEstimate(
    val foodName: String,
    val estimatedWeightG: Double,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val confidence: Double
)

class MainActivity : ComponentActivity() {

    companion object {
        private  val ANALYZE_URL =
            "https://al-kitchen-scale-production.up.railway.app/analyze"
        override fun onCreate(savedInstanceState: Bundle?) {
     super.onCreate(savedInstanceState)

   setContent {
            MaterialTheme {
                KitchenScaleScreen()
            }
        }
    }
   @Composable
    private fun KitchenScaleScreen() {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
         var estimate by remember { mutableStateOf<FoodEstimate?>(null) }
       
 var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

         val scope = rememberCoroutineScope()

        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            selectedImageUri = uri
            estimate = null
            errorMessage = null
        }

    Column(
        modifier = Modifier
.fillMaxSize()
.verticalScroll(rememberScrollState())
.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
 horizontalAlignment = Alignment.CenterHorizontally
 ) {

Text(
  text = "AI Kitchen Scale",
 style = MateriaITheme.typography.headlineMedium
)

 Text(
.text = "Избери слика од храната и анализирај.",
)

            Button(
                onClick = { imagePicker.launch("image/*") },
 modifier = Modifier.fillMaxWidth()
  ) {
 Text("Избери слика")
  }

            selectedImageUri?.let { uri ->

 val bitmap = remember(uri) {
  try {
contentResolver.openInputStream(uri)?.use                             {BitmapFactory.decodeStream(it)
}
 } catch (e: Exception) {
null
}
}

bitmap?.let {
Image(
 bitmap = it.asImageBitmap(),
                        contentDescription = "Food image",
 modifier = Modifier
.fillMaxWidth()
 .heightIn(max = 320.dp)
 )                            }
            }

            Button(
 onClick = {

val uri = selectedImageUri ?: return@Button

 scope.launch {

.isLoading = true
 errorMessage = null
                        estimate = null

 val result = withContext(Dispatchers.IO) {
 analyzeImage(uri)
}

if (result != null) {
estimate = result
} else {                        {
errorMessage =
"Анализата не успеа."
 }

  isLoading = false
  }
  },
 enabled = selectedImageUri != null && !isLoading,
                modifier = Modifier.fillMaxWidth()
 ) {
 Text("Анализирај")
 }

 if (isLoading) {
 CircularProgressIndicator()
  Text("Се анализира...")
            }

errorMessage?.let {
Text(it)
}

estimate?.let {
ResultCard(it)
}
 }
}
  @Composable
    private fun ResultCard(item: FoodEstimate) {
Card(
modifier = Modifier.fillMaxWidth()
) {
Column(
  modifier = Modifier.padding(18.dp),
verticalArrangement = Arrangement.spacedBy(8.dp)
) {
Text(
text = item.foodName,
 style = MaterialTheme.typography.titleLarge
)
Text("Тежина: ${item.estimatedWeightG.toInt()} g")
 Text("Калории: ${item.calories.toInt()} kcal")
Text("Протеини: ${item.proteinG} g")
 Text("Јаглехидрати: ${item.carbsG} g")
 Text("Масти: ${item.fatG} g")
 Text("Сигурност: ${(item.confidence * 100).toInt()}%")
}
}}


 private fun analyzeImage(uri: Uri): FoodEstimate? {

return try {

val bytes = contentResolver
   .openInputStream(uri)
?.readBytes()
 }?: return null

  val mimeTipe= contentResolver.getType(uri) ?:"image/jpeg" val base64 = Base64.encodeToString(
 bytes,
Base64.NO_WRAP
 )

 val requestJson = JSONObject()
.put("imageBase64", base64)
.put("mimeType", mimeType)
put("language","mk")

 val connection =
URL(ANALYZE_URL)
.openConnection() as HttpURLConnection).apply{

requestMethod = "POST" connectTimeout = 30_000 readTimeout = 90_000 
doOutput = true
 set RequestProperty("content-Type","application/json") 
           setRequestProperty("Accept","application/json").apply {...}. connection.outputStream.use { output-> output.write(requestJson.toString().toByteArray(Charsets.UTF_8)) } connection.outputStream.use { output  val statusCode = connection.responseCode val stream = if (statusCode in 200..299) connection.inputStream.else connection.errorStream 

                          val body = stream?.bufferedReader() } ?:return null if (statusCode ! in 200..299) { return null  if (sttusCode !in 200..299) {  val json = JSONObject(body) FoodEstimate( foodName = json.optString("foodname", Food"), estimatedweightG",0.0),calories = json.optDouble("calories", 0.0),proteinG", 0.0),carbsG = json.optDouble("carbsG",0.0), fatG = json.optDouble("fatG", 0.0), confidence" = json.optDouble("confidence", 0.0) ,) } catch(_:Exception) { null } } }           
                
                                                    

                
 
