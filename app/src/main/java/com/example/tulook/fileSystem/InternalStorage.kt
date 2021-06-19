package com.example.tulook.fileSystem
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class InternalStorage {
    companion object {

        fun getFiles(context: Context): Array<out File>? {
            return File(context.filesDir.path).listFiles()
        }

        fun getFileUri(context: Context, fileName: String): String? {
            val file = File(context.filesDir.path + "/" + fileName)
            if (file.exists()) {
                return file.toURI().toString()
            }
            return null
        }

        fun saveFile(context: Context, text: String, fileName: String) {
            val file = File(context.filesDir.path + "/" + fileName)
            try {
                if(!getFiles(context)?.any { it == file }!!){
                    Log.e("INTERNAL STORAGE", "El archivo no existia y se creo")
                    file.createNewFile()
                }
                val ostream = FileOutputStream(file)
                ostream.write(text.toByteArray())
                ostream.flush()
                ostream.close()
            } catch (e: IOException) {
                Log.e("IOException", e.localizedMessage)
            }

        }

        fun readFile(context: Context, fileName: String): String {
            val file = File(context.filesDir.path + "/" + fileName)
            var content = ""
            try{
                val istream = FileInputStream(file)
                content = istream.readBytes().toString(Charsets.UTF_8)
                Log.e("INTERNAL STORAGE", "Este es el contenido del archivo: " + content)
                istream.close()
            }catch (e: IOException) {
                Log.e("IOException", e.localizedMessage)
            }
            return content
        }

        fun deleteFile(context: Context, fileName: String) {
            val file = File(context.filesDir.path + "/" + fileName)
            if (file.exists()) {
                file.delete()
            }
        }

        fun getCacheFileUri(context: Context, fileName: String): String? {
            val file = File(context.cacheDir.path + "/" + fileName)
            if (file.exists()) {
                return file.toURI().toString()
            }
            return null
        }

        fun saveFileInCache(context: Context, bitmap: Bitmap, fileName: String) : File {
            val file = File(context.cacheDir.path + "/" + fileName)
            try {
                file.createNewFile()
                val ostream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream)
                ostream.flush()
                ostream.close()
            } catch (e: IOException) {
                Log.e("IOException", e.localizedMessage)
            }

            return file
        }

        fun deleteFileFromCache(context: Context, fileName: String) {
            val file = File(context.filesDir.path + "/" + fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}