package com.example.snake.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
const val background = 0
const val wall = 1
const val snake = 2
const val apple = 3
fun print(obj: String)
{
    Log.d("Snake Debugging",obj)
}
class Position(var x: Int, var y: Int) {}
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
    var inputQueue: Queue<Float> = LinkedList()
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
        inputQueue.add(amount)
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
        print("Apple pos " + appleX.toString() + " " + appleY.toString())
    }

    fun processMovement(inputQueue: Queue<Float>)
    {
        if (inputQueue.isNotEmpty())
        {
            val amount = inputQueue.first()
            inputQueue.remove()

            // Rotate
            if (amount >= 0) headAngle += 90
            else headAngle -= 90

            // Bound correction
            if (headAngle == -90.0) headAngle = 270.0
            if (headAngle == 360.0) headAngle = 0.0

        }
    }

    fun displayScore(score: Int)
    {
        val scoreString = score.toString()
        var x = 21/2 - 1
        var y = 21/2 - 2

        for (char in scoreString)
        {
            drawSplash(this,char.toString(),x,y)
            x += 4
        }
    }

    var model = this
    init {
        viewModelScope.launch {
            // The main loop
            while (true)
            {
                initGrid()
                var highScore = 0

                try {
                    highScore = FileHandling.getFileContents().split(";")[0].split(",")[1].toInt()
                }
                catch (e: Exception) { Log.d("File handling error: ",FileHandling.getFileContents()) }

                var currentScore = 0

                displayScore(highScore)
                delay(1000)

                initGrid()
                placeApple()

                var alive = true
                while (alive)
                {
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

                        if (currentScore > highScore)
                        {
                            highScore = currentScore
                            FileHandling.writeToFile("HighScore,"+highScore.toString()+";")
                        }
                        Log.d("High score",highScore.toString())
                        currentScore = 0

                        initGrid()
                        setPixel(appleX,appleY,background)
                        placeApple()
                        headX = numSquares / 2
                        headY = numSquares / 2
                        alive = false
                        continue;
                    }

                    // draw snake
                    setPixel(headX,headY,snake)
            }
            }
        }
    }
}
