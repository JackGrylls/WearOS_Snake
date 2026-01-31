package com.example.snake.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.storage.StorageManager
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import com.example.snake.presentation.theme.SnakeTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.Executors

val backgroundCol = Color.hsv(72f,50f/100,0.5f)
val wallCol = Color.hsv(72f,50f/100,0.1f)
val snakeCol = Color.hsv(72f,50f/100,0.1f)
val appleCol = Color.Red

// the "main" class which runs when the app starts
// Very simply, this handles I/O
class MainActivity : ComponentActivity() {
    lateinit var viewModel: GameViewModel

    override fun onPause()
    {
        super.onPause()
        Log.d("MainActivity","Game paused!")
        viewModel.pauseGame()
    }

    override fun onResume()
    {
        super.onResume()
        Log.d("MainActivity","Game resumed!")
        if (this::viewModel.isInitialized) viewModel.resumeGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        // We initialise the data store for high scores and settings
        Log.d("MainActivity","H")
        FileHandling.InitDataStore(this)
        Log.d("MainActivity","HH")
        var fileContents = FileHandling.getFileContents()

        // viewModel is the actual game logic (Game.kt)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            viewModel = viewModel()
            WearApp (
                viewModel = viewModel
            )
        }
    }
}

// Handles drawing to the screen and input events
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
//                    if (col == backgroundCol) continue; // skip rendering background pixels

                    val xCor : Float = size.width / (numSquares) * x + 1
                    val yCor : Float = size.height / (numSquares) * y + 1

                    var cornerRadius = 2f
                    if (viewModel.logicGrid[x][y] == snake) cornerRadius = 5f

                    drawRoundRect(
                        color = col,
                        size = Size(squareSize,squareSize),
                        topLeft = Offset(xCor,yCor),
                        cornerRadius = CornerRadius(cornerRadius,cornerRadius)
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