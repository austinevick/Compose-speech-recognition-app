package com.austinevick.speechrecognitionapp.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.austinevick.speechrecognitionapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.speechUiState.collectAsState()
    val prompt = remember { mutableStateOf("") }

    val infiniteTransition =
        rememberInfiniteTransition(label = "infiniteTransition")
    val animateColor = infiniteTransition.animateColor(
        initialValue = Color(0xffe5ecf9).copy(0.5f),
        targetValue = Color(0xff0044c0).copy(0.8f),
        animationSpec = infiniteRepeatable(
            animation =
            tween(2000)
        ), label = "colorAnimation"
    )
    val scale = infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000)
        ), label = "scaleAnimation"
    )


    LaunchedEffect(uiState.value.prompt) {
        if (uiState.value.prompt.isNotEmpty()) {
            viewModel.generateResponse(uiState.value.prompt)
        }
    }

    if (uiState.value.response.isNotEmpty()) {
        LaunchedEffect(uiState.value.response) {
            viewModel.textToSpeechHandler(uiState.value.response)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Speech Recognition App")
            },
                actions = {
                    IconButton(onClick = {
                        viewModel.stopTextToSpeech()
                    }) {
                        Icon(painterResource(R.drawable.record_voice_over_24),
                            contentDescription = null)
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

           if (prompt.value.isEmpty()) Text(
                if (uiState.value.prompt.isEmpty())
                    "Your speech will appear here"
                else uiState.value.prompt.substring(0).toUpperCase(),
                color =if (uiState.value.prompt.isEmpty()) Color.Gray else Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (prompt.value.isEmpty())   HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            Text(uiState.value.response,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))

          if (uiState.value.isLoading) CircularProgressIndicator() else
              Box(contentAlignment = Alignment.Center,
                modifier = Modifier.clip(CircleShape)) {
                Box(modifier = Modifier
                    .scale(scale.value)
                    .size(150.dp)
                    .background(animateColor.value, CircleShape)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Black, CircleShape)
                        .size(80.dp)
                ) {
                    Image(
                        painterResource(id = R.drawable.baseline_mic_24),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                               viewModel.startListening()
                            }, colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null
                    )
                }
            }

            TextField(prompt.value,
                onValueChange = {prompt.value = it},
                placeholder = {
                    Text("Ask me anything",color = Color.Gray,
                        fontSize = 16.sp, fontWeight = FontWeight.Medium)
                },
                trailingIcon = {
                    FilledIconButton(
                        enabled = prompt.value.isNotEmpty(),
                        onClick = {
                        viewModel.generateResponse(prompt.value)
                            viewModel.setPrompt(prompt.value)
                            prompt.value = ""
                    }) {
                        Icon(Icons.Default.Send, contentDescription = null)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.padding(top = 16.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedIndicatorColor = Color.White,
                    cursorColor = Color.Black)
                )

        }
    }


}