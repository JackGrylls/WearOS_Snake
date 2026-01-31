package com.example.snake.presentation
import com.example.snake.R
import android.content.res.Resources
import android.util.Log

val splashes = arrayOf(
    "0;3,5;000000111000000",
    "1;3,5;011100000011110",
    "2;3,5;010000101000010",
    "3;3,5;010100101000000",
    "4;3,5;000111101100000",
    "5;3,5;000100101001000",
    "6;3,5;000000101001000",
    "7;3,5;011000101100111",
    "8;3,5;000000101000000",
    "9;3,5;000100101000000"
)

fun drawSplash(context: GameViewModel, name: String, x: Int, y: Int)
{
    for (splash in splashes)
    {
        val data =  splash.split(";")
        val filename = data[0]
        val width = data[1].split(",")[0].toInt()
        val height = data[1].split(",")[1].toInt()
        val pixels = data[2]

        if (filename != name) continue;

        var count = 0
        for (i in (0..width - 1))
        {
            for (j in 0..height - 1)
            {
                if (pixels[count] == '0') context.setPixel(x+i,y+j,wall)
                if (pixels[count] == '1') context.setPixel(x+i,y+j,background)
                count++
            }
        }
    }
}