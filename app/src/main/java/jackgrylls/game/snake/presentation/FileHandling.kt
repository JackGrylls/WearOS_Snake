package jackgrylls.game.snake.presentation

import android.content.Context
import android.os.storage.StorageManager
import android.util.Log
import java.util.concurrent.Executors
import kotlin.collections.contains

class FileHandling {
    companion object {
        const val filename = "SnakeDataStore"
        const val defaultFileContents = "HighScore,0,0,0;Settings,"
        const val logger = "SNAKE FILE HANDLING"

        var busyFlag = false
        lateinit var context: MainActivity

        // SETTINGS FUNCTIONS
        fun getSettings(): String
        {
            return getFileContents().split(";")[1]
        }

        // HIGH SCORE FUNCTIONS
        fun setHighScore(newScore: Int)
        {
            var newScore = newScore
            val scores = getHighScores()
            var newStr = "HighScore,"

            for (scoreStr in scores)
            {
                Log.d(logger,"$scoreStr $newScore")
                val score = scoreStr.toInt()
                if (newScore > score)
                {
                    newStr += "$newScore,"
                    newScore = score // Set to 0 so that the following entries are not set
                }
                else newStr += "$score,"
            }

            // The above for loop adds an extra comma, it's easier to remove it afterwards than add logic to avoid adding it
            Log.d(logger,newStr)
            newStr = newStr.removeSuffix(",")
            Log.d(logger,newStr)

            val settings = getSettings()
            writeToFile("$newStr;$settings")
        }

        fun getHighScores(): List<String>
        {
            val scores = getFileContents()
                .split(";")[0] // Separate high scores from settings
                .split(",").toMutableList()

            scores.removeAt(0)
            for (score in scores)
            {
                Log.d(logger,"Score: $score")
            }

            return scores.toList()
        }
        fun dataStoreExists(): Boolean
        {
            return context.fileList().contains(filename)
        }
        fun getFileContents(): String
        {
            while (busyFlag) {/* wait until not busy */}
            if (!dataStoreExists()) return "FILE ERROR";
            val fileContents = context.openFileInput(filename)
                .bufferedReader()
                .use { it.readText() }

            return fileContents
        }
        fun writeToFile(data: String): Boolean
        {
            Log.d(logger,"SETTING BUSY FLAG")
            busyFlag = true
            val bytesRequired = data.toByteArray().size.toLong()
            val storageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            Executors.newSingleThreadExecutor().execute {
                val bytesAvailable =
                    storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT)

                if (bytesRequired > bytesAvailable)
                {
                    Log.d(logger, "File handler ran out of space!$bytesRequired $bytesAvailable")
                }
                else
                {
                    context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it.write(data.toByteArray())
                    }
                    Log.d(logger, "Successfully wrote data:\n$data")
                    busyFlag = false
                }
            }
            return true
        }

        // Checks for the existence of the data store. If it doesn't exist, create it.
        fun InitDataStore(mainActivity: MainActivity)
        {
            this.context = mainActivity

            if (!dataStoreExists())
            {
                Log.d(logger,"Data store not found, creating")

                writeToFile(defaultFileContents)

                Log.d(logger,"Created data store")
            }
            else
            {
                Log.d(logger,"Found existing data store")
            }
        }

        fun deleteDataStore()
        {
            context.deleteFile(filename)
            Log.d(logger,"Removed data store")
        }

        fun resetDataStore()
        {
            Log.d(logger,"Reset data store")
            writeToFile(defaultFileContents)
        }
    }
}