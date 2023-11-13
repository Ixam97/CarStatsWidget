package de.ixam97.carstatswidget.util

import android.graphics.Bitmap
import android.util.Log

sealed interface ResizeBitmap {
    object Height : ResizeBitmap
    object Width : ResizeBitmap
}

fun getAspectRatio(image: Bitmap): Float {
    val width = image.width
    val height = image.height
    return width.toFloat() / height.toFloat()
}

fun getResizedBitmap(image: Bitmap, maxValue: Int, dimension: ResizeBitmap): Bitmap {
    var width = image.width
    var height = image.height
    val bitmapRatio = width.toFloat() / height.toFloat()
    when (dimension) {
        is ResizeBitmap.Height -> {
            height = maxValue
            width = (height * bitmapRatio).toInt()
        }
        is ResizeBitmap.Width -> {
            width = maxValue
            height = (width / bitmapRatio).toInt()
        }
    }
    return Bitmap.createScaledBitmap(image, width, height, true)
}