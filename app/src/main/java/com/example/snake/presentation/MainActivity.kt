/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.snake.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.snake.R
import com.example.snake.presentation.theme.SnakeTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel()
{
    val numSquares = 25
    val spacing = 2
    val pixels = mutableStateListOf<Color>().apply {
        repeat(numSquares*numSquares)
        {
            add(Color.Blue)
        }
    }

    fun setPixel(x: Int, y: Int, color: Color)
    {
        pixels[y * numSquares + x] = color
    }

    init {
        viewModelScope.launch {
            while (true)
            {
                delay(100)
                setPixel(5,5,Color.Red)
                delay(100)
                setPixel(5,5,Color.Green)
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val viewModel: GameViewModel = viewModel()
            WearApp(
                pixels = viewModel.pixels,
                numSquares = viewModel.numSquares,
                spacing = viewModel.spacing
            )
        }
    }
}

@Composable
fun WearApp(pixels: List<Color>, numSquares: Int, spacing: Int) {
    SnakeTheme {
        Canvas(modifier = Modifier.fillMaxSize())
        {
            var squareSize = (size.width - ((numSquares-1)*spacing))/numSquares;

            for (x in 0..numSquares - 1)
            {
                for (y in 0..numSquares - 1)
                {
                    val xCor : Float = size.width / (numSquares) * x
                    val yCor : Float = size.height / (numSquares) * y
                    drawRect (
                        color = pixels[y * numSquares + x],
                        size = Size(squareSize,squareSize),
                        topLeft = Offset(xCor,yCor)
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
//    WearApp("Preview Android")
}