package jackgrylls.game.snake.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.text.iterator

const val background = 0
const val wall = 1
const val snake = 2
const val apple = 3
const val logger = "SNAKE GAME LOOP"
class Position(var x: Int, var y: Int) {}

class Input(var bezelDelta: Float, var tapX: Float, var tapY: Float, var width: Int, var height: Int)
class GameViewModel : ViewModel()
{
    val numSquares = 21; val spacing = 2
    val pixels = mutableStateListOf<Color>().apply {
        repeat(numSquares*numSquares)
        {
            add(backgroundCol)
        }
    }
    var colors = listOf(
        backgroundCol,
        wallCol,
        snakeCol,
        appleCol
    )

    var logicGrid = Array(numSquares) {Array<Int>(numSquares) { 0 } }
    var headAngle: Double = 0.0; var headX = numSquares / 2; var headY = numSquares / 2;
    var appleX = 0; var appleY = 0
    var snakeQueue: Queue<Position> = LinkedList()
    var inputQueue: Queue<Input> = LinkedList()
    var snakeLength = 3
    var tickDelay = 200

    fun setPixel(x: Int, y: Int, value: Int)
    {
        logicGrid[x][y] = value
        pixels[y * numSquares + x] = colors[value];
    }

    fun initGrid()
    {
        val centre: Float = numSquares / 2f - 0.5f
        val radius: Float = centre - 0.5f
        for (x in 0..numSquares - 1)
        {
            for (y in 0..numSquares - 1)
            {
                val xCor = x - centre
                val yCor = y - centre
                if ((xCor*xCor) + (yCor*yCor) > radius * radius) setPixel(x,y,wall)
                else setPixel(x,y,background)
            }
        }
    }

    fun onRotate(amount: Float)
    {
        Log.d(logger,"ROTATE EVENT")
        var input: Input = Input(amount,0f,0f,0,0)
        inputQueue.add(input)
        if (inputQueue.size > 2)
        {
            inputQueue.remove(inputQueue.last())
        }
    }

    fun onTap(x: Float, y: Float, width: Int, height: Int)
    {
        Log.d(logger,"TAP EVENT")
        var input: Input = Input(0f,x,y, width, height)
        inputQueue.add(input)
        if (inputQueue.size > 2)
        {
            inputQueue.remove(inputQueue.last())
        }
    }

    fun placeApple()
    {
        // Initial selection
        appleX = (0..numSquares - 1).random()
        appleY = (0..numSquares - 1).random()

        // If not valid keep trying
        while (logicGrid[appleX][appleY] == wall || logicGrid[appleX][appleY] == snake)
        {
            appleX = (0..numSquares - 1).random()
            appleY = (0..numSquares - 1).random()
        }
        setPixel(appleX,appleY,apple)
        Log.d(logger,"Apples pos $appleX $appleY")
    }

    fun processMovement(inputQueue: Queue<Input>)
    {
        if (inputQueue.isNotEmpty())
        {
            Log.d(logger,"PROCESSING INPUT")
            val input = inputQueue.first()
            inputQueue.remove()

            // Bezel input
            if (input.bezelDelta != 0f)
            {
                // Rotate
                if (input.bezelDelta >= 0) headAngle += 90
                else headAngle -= 90
            }
            // Touch input
            else
            {
                val x = input.tapX;
                val y = input.tapY;

                val width = input.width;
                val height = input.height;

                when (headAngle) {
                    0.0 -> if (y > height / 2) headAngle += 90 else headAngle -= 90
                    90.0 -> if (x > width / 2) headAngle -= 90 else headAngle += 90
                    180.0 -> if (y > height / 2) headAngle -= 90 else headAngle += 90
                    270.0 -> if (x > width / 2) headAngle += 90 else headAngle -= 90
                }
            }

            // Bound correction
            if (headAngle == -90.0) headAngle = 270.0
            if (headAngle == 360.0) headAngle = 0.0

        }
    }

    fun pauseGame()
    {
        Log.d(logger,"GAME PAUSED")
        isPaused.value = true
    }

    fun resumeGame()
    {
        Log.d(logger,"GAME RESUMED")
        isPaused.value = false
    }

    fun displayHighScores(scoreIndex: Int)
    {
        var scores = FileHandling.getHighScores()
        val scoreString = scores[scoreIndex-1]

        var x = 21/2 - 9 / 2 // half of display size minus size of hash and number
        var y = 21/2 - 6
        drawSplash(this,"hash",x,y)
        x += 6
        drawSplash(this,scoreIndex.toString(),x,y)
        for (i in 21/2-5..21/2+5)
        {
            setPixel(i,21/2,wall)
        }


        // Decides where to start drawing text from based on digit length
        // (So the numbers can be centred)
        val textWidth = 4 * scores[scoreIndex-1].length - 1
        x = 21/2 - textWidth / 2
        y = 21/2 + 2

        for (char in scoreString)
        {
            drawSplash(this,char.toString(),x,y)
            x += 4
        }
    }

    fun displayScore(score: String)
    {
        val scoreString = score

        // Decides where to start drawing text from based on digit length
        // (So the numbers can be centred)
        val textWidth = 4 * score.length - 1
        var x = 21/2 - textWidth / 2
        var y = 21/2 - 2

        for (char in scoreString)
        {
            drawSplash(this,char.toString(),x,y)
            x += 4
        }
    }
    private val isPaused = MutableStateFlow(false);

    init {
        viewModelScope.launch {
            // The main loop
            while (true)
            {
                isPaused.filter {paused -> !paused}.first()
                initGrid()

                var currentScore = 0
                var highScore = FileHandling.getHighScores()[0]

                for (i in 1..3)
                {
                    displayHighScores(i)
                    delay(1500)
                    initGrid()
                }

                initGrid()
                placeApple()

                var alive = true
                while (alive)
                {
                    isPaused.filter {paused -> !paused}.first()
                    delay(tickDelay.toLong())

                    // apple collision check
                    if (headX == appleX && headY == appleY)
                    {
                        snakeLength++
                        currentScore++
                        placeApple()
                    }

                    // update head angle
                    processMovement(inputQueue)

                    // recalculate velocity, move head
                    var veloX = cos(headAngle / 180 * PI)
                    var veloY = sin(headAngle / 180 * PI)
                    headX = (headX + veloX.roundToInt()).mod(numSquares)
                    headY = (headY + veloY.roundToInt()).mod(numSquares)

                    // add new head to snake
                    snakeQueue.add(Position(headX,headY))

                    // remove tail
                    if (snakeQueue.size > snakeLength)
                    {
                        var first = snakeQueue.first()
                        setPixel(first.x,first.y,background)
                        snakeQueue.remove()
                    }

                    // wall collision check
                    if (logicGrid[headX][headY] == wall || logicGrid[headX][headY] == snake)
                    {
                        snakeLength = 3
                        while (snakeQueue.isNotEmpty())
                        {
                            var first = snakeQueue.first()
                            setPixel(first.x,first.y,background)
                            snakeQueue.remove()
                        }

                        FileHandling.setHighScore(currentScore)

                        initGrid()
                        setPixel(appleX,appleY,background)
                        headX = numSquares / 2
                        headY = numSquares / 2
                        alive = false
                        displayScore(currentScore.toString())
                        delay(1500)
                        currentScore = 0
                        continue;
                    }

                    // draw snake
                    setPixel(headX,headY,snake)
                }
            }
        }
    }
}
