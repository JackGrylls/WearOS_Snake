package com.example.snake.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import com.example.snake.presentation.theme.SnakeTheme
import androidx.lifecycle.viewmodel.compose.viewModel
val backgroundCol = Color.hsv(72f,50f/100,0.5f)
val filledCol = Color.hsv(72f,50f/100,0.1f)
val appleCol = Color.Red

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            val viewModel: GameViewModel = viewModel()
            WearApp (
                viewModel = viewModel
            )
        }
    }
}
@Composable
fun WearApp(viewModel: GameViewModel) {
    val focusRequester = remember { FocusRequester() }
    SnakeTheme {
        Canvas(modifier = Modifier.fillMaxSize()) { drawRect(color = backgroundCol) }
        Canvas(
            modifier = Modifier.fillMaxSize()
        )
        {
            val pixels = viewModel.pixels
            val spacing = viewModel.spacing
            val numSquares = viewModel.numSquares
            var squareSize = (size.width - ((numSquares-1)*spacing))/numSquares;

            for (x in 0..numSquares - 1)
            {
                for (y in 0..numSquares - 1)
                {
                    val col = pixels[y*numSquares + x]
                    if (col == backgroundCol) continue; // skip rendering background pixels

                    val xCor : Float = size.width / (numSquares) * x + 1
                    val yCor : Float = size.height / (numSquares) * y + 1
                    drawRect (
                        color = col,
                        size = Size(squareSize,squareSize),
                        topLeft = Offset(xCor,yCor)
                    )
                }
            }
        }
        Canvas(
            modifier = Modifier.onRotaryScrollEvent {
                    viewModel.onRotate(it.verticalScrollPixels)
                    true
                }
                .focusRequester( focusRequester )
                .focusable()
        )

        {

        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}