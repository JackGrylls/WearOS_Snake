/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.snake.presentation

import android.os.Bundle
import android.os.Debug
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.fromColorLong
import androidx.compose.ui.input.rotary.RotaryScrollEvent
import androidx.compose.ui.input.rotary.onPreRotaryScrollEvent
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onPlaced
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

val backgroundCol = Color.hsv(72f,50f/100,0.5f)
val filledCol = Color.hsv(72f,50f/100,0.1f)
class GameViewModel : ViewModel()
{
    val numSquares = 25
    val spacing = 2
    val pixels = mutableStateListOf<Color>().apply {
        repeat(numSquares*numSquares)
        {
            add(backgroundCol)
        }
    }

    var expiry = Array(numSquares) { Array<Int>(numSquares) { 0 } }

    var headX = numSquares / 2
    var headY = numSquares / 2
    var headAngle: Double = 0.0
    fun setPixel(x: Int, y: Int, color: Color)
    {
        pixels[y * numSquares + x] = color
    }

    fun initGrid()
    {
        var centre: Float = numSquares / 2f - 0.5f
        var radius: Float = centre - 0.5f
        for (x in 0..numSquares - 1)
        {
            for (y in 0..numSquares - 1)
            {
                var xCor = x - centre
                var yCor = y - centre
                if ((xCor*xCor) + (yCor*yCor) > radius * radius) expiry[x][y] = -1
            }
        }
    }
    fun updateGrid()
    {
        for (x in 0..numSquares - 1)
        {
            for (y in 0..numSquares - 1)
            {
                if (expiry[x][y] > 0)
                {
                    expiry[x][y] -= 1
                }
                if (expiry[x][y] == 0) setPixel(x,y,backgroundCol)
                else
                {
                    setPixel(x,y,filledCol)
                }
            }
        }
    }
    fun onRotate(amount: Float)
    {
        // Rotate
        if (amount >= 0) headAngle += 90
        else headAngle -= 90

        // Bound correction
        if (headAngle == -90.0) headAngle = 270.0
        if (headAngle == 360.0) headAngle = 0.0

    }

    init {
        viewModelScope.launch {
            // The main game loop
            while (true)
            {
                delay(100) // tick rate
                var veloX = cos(headAngle / 180 * PI)
                var veloY = sin(headAngle / 180 * PI)
                setPixel(headX,headY,filledCol)
                if (expiry[headX][headY] >= 0 ) expiry[headX][headY] = 10
                headX = (headX + veloX.roundToInt()).mod(25)
                headY = (headY + veloY.roundToInt()).mod(25)
                updateGrid()
            }
        }
    }
}

fun print(obj: String)
{
    Log.d("Snake Debugging",obj)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val viewModel: GameViewModel = viewModel()
            viewModel.initGrid()
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
                    val xCor : Float = size.width / (numSquares) * x + 1
                    val yCor : Float = size.height / (numSquares) * y + 1
                    drawRect (
                        color = pixels[y * numSquares + x],
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
//    WearApp("Preview Android")
}