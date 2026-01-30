package com.example.snake.presentation

import android.content.Context
import android.os.storage.StorageManager
import android.util.Log
import java.util.concurrent.Executors
import kotlin.collections.contains

class FileHandling {
    companion object {
        const val filename = "SnakeDataStore"
        const val fileContents = "HighScore,0;"
        const val logger = "SNAKE FILE HANDLING"

        lateinit var context: MainActivity

        fun getFileContents(): String
        {
            val fileContents = context.openFileInput(filename)
                .bufferedReader()
                .use { it.readText() }

            return fileContents
        }
        fun writeToFile(data: String): Boolean
        {
            val bytesRequired = data.toByteArray().size.toLong()

            val storageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

            Executors.newSingleThreadExecutor().execute {
                val bytesAvailable =
                    storageManager.getAllocatableBytes(StorageManager.UUID_DEFAULT)

                if (bytesRequired > bytesAvailable)
                {
                    Log.d(logger,"File handler ran out of space!"+bytesRequired.toString()+" "+bytesAvailable.toString())
                }
                else
                {
                    context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it.write(data.toByteArray())
                    }
                }
            }

            return true
        }

        // Checks for the existence of the data store. If it doesn't exist, create it.
        fun InitDataStore(mainActivity: MainActivity)
        {
            this.context = mainActivity

            if (!context.fileList().contains(filename))
            {
                Log.d(logger,"Data store not found, creating")

                writeToFile(fileContents)

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
            writeToFile(fileContents)
        }
    }
}