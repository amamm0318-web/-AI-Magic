package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
  fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 1280): String? {
    return try {
      val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
      val originalBitmap = BitmapFactory.decodeStream(inputStream)
      inputStream?.close() ?: return null

      // Scale down if exceeds maxDimension
      val width = originalBitmap.width
      val height = originalBitmap.height
      val scaledBitmap = if (width > maxDimension || height > maxDimension) {
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
          newWidth = maxDimension
          newHeight = (maxDimension / ratio).toInt()
        } else {
          newHeight = maxDimension
          newWidth = (maxDimension * ratio).toInt()
        }
        Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
      } else {
        originalBitmap
      }

      val outputStream = ByteArrayOutputStream()
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
      val byteArray = outputStream.toByteArray()
      Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
      val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
      null
    }
  }

  fun base64ToByteArray(base64Str: String): ByteArray? {
    return try {
      Base64.decode(base64Str, Base64.DEFAULT)
    } catch (e: Exception) {
      null
    }
  }

  fun saveBase64ToCacheFile(
    context: Context,
    base64Str: String,
    fileName: String = "gemini_img_${System.currentTimeMillis()}.jpg"
  ): java.io.File? {
    return try {
      val bytes = Base64.decode(base64Str, Base64.DEFAULT)
      val dir = java.io.File(context.cacheDir, "gemini_images")
      if (!dir.exists()) dir.mkdirs()
      val file = java.io.File(dir, fileName)
      file.writeBytes(bytes)
      file
    } catch (e: Exception) {
      null
    }
  }
}
