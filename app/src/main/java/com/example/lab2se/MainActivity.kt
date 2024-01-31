package com.example.lab2se

import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lab2se.model.Home
import com.example.lab2se.ui.theme.Lab2seTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource

class MainActivity : ComponentActivity() {
    private val RECORD_AUDIO_PERMISSION_CODE = 1
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Lab2seTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    HomeLayout(startListening = { startListening() })
                }
            }
        }
    }

    private fun startListening() {
        while (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListenerAdapter() {
                override fun onResults(results: Bundle) {
                    val matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        Log.d("SPEECH", matches[0])
                        handleSpeechCommand(matches[0])
                    }
                }
            })
        }

        // Start listening
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    private fun handleSpeechCommand(command: String) {
        val database = Firebase.database("https://lab2se-e06d1-default-rtdb.firebaseio.com/")
        val myRef = database.getReference("home")

        when {
            "light on" in command.lowercase() -> {
                myRef.child("light").setValue(1)
                Toast.makeText(this, "Turning on the light", Toast.LENGTH_SHORT).show()
            }
            "light off" in command.lowercase() -> {
                myRef.child("light").setValue(0)
                Toast.makeText(this, "Turning off the light", Toast.LENGTH_SHORT).show()
            }
            "open door" in command.lowercase() -> {
                myRef.child("door").setValue(1)
                Toast.makeText(this, "Opening the door", Toast.LENGTH_SHORT).show()
            }
            "close door" in command.lowercase() -> {
                myRef.child("door").setValue(0)
                Toast.makeText(this, "Closing the door", Toast.LENGTH_SHORT).show()
            }
            "open window" in command.lowercase() -> {
                myRef.child("window").setValue(1)
                Toast.makeText(this, "Opening the window", Toast.LENGTH_SHORT).show()
            }
            "close window" in command.lowercase() -> {
                myRef.child("window").setValue(0)
                Toast.makeText(this, "Closing the window", Toast.LENGTH_SHORT).show()
            }
            else -> Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(startListening: () -> Unit) {
    val lightStatus = remember { mutableStateOf(false) }
    val doorStatus = remember { mutableStateOf(false) }
    val windowStatus = remember { mutableStateOf(false) }

    val database = Firebase.database("https://lab2se-e06d1-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("home")

    LaunchedEffect(true){
        myRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Home>()
                if (value != null) {
                    Log.d(TAG, "Value is: $value")
                    lightStatus.value = value.light == 1
                    doorStatus.value = value.door == 1
                    windowStatus.value = value.window == 1
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    Column {
        TopAppBar(
            title = { Text(text = "Smart Home", fontSize = 20.sp) },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = Color(red = 0, green = 20, blue = 39),
                titleContentColor = Color.White
            ),
        )
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ControlCard(title = "LAMP", status = lightStatus) {
                    lightStatus.value = it
                    myRef.child("light").setValue(if (it) 1 else 0)
                }
                ControlCard(title = "DOOR", status = doorStatus) {
                    doorStatus.value = it
                    myRef.child("door").setValue(if (it) 1 else 0)
                }
                ControlCard(title = "WINDOW", status = windowStatus) {
                    windowStatus.value = it
                    myRef.child("window").setValue(if (it) 1 else 0)
                }
            }
            Button(
                onClick = { startListening() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mic_icon),
                    contentDescription = "Mic",
                    tint = Color.Yellow,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ControlCard(title: String, status: MutableState<Boolean>, toggle: (Boolean) -> Unit) {
    Surface(
        color = Color.Gray,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(
                        text = "${title}: ",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                    if (title == "LAMP") {
                        Text(text = if (status.value) "ON" else "OFF")
                    } else {
                        Text(text = if (status.value) "OPEN" else "CLOSED")
                    }
                }
                // Icons
                val iconRes = if (title == "LAMP") {
                    if (status.value) R.drawable.lamp_2 else R.drawable.lamp_1
                } else if (title == "DOOR") {
                    if (status.value) R.drawable.door_2 else R.drawable.door_1
                } else {
                    if (status.value) R.drawable.window_2 else R.drawable.window_1
                }
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Switch(
                    checked = status.value,
                    onCheckedChange = toggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color.DarkGray,
                        checkedThumbColor = Color.Yellow,
                        uncheckedTrackColor = Color.LightGray,
                        uncheckedThumbColor = Color.DarkGray,
                    )
                )
            }
        }
    }
}