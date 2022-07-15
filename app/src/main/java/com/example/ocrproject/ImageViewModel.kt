package com.example.ocrproject

import android.graphics.*
import androidx.lifecycle.ViewModel
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class ImageViewModel : ViewModel() {
    var isCamScanned:Boolean = false

    fun changeBitmapContrastBrightness(bmp: Bitmap, contrast: Float, brightness: Float): Bitmap {
        val cm = ColorMatrix(
            floatArrayOf(contrast,0f,0f,0f,brightness,0f,contrast,0f,0f,brightness,0f,0f,contrast,
                0f,brightness,0f,0f,0f,1f,0f))
        val ret = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
        val canvas = Canvas(ret)
        val paint = Paint()
        paint.setColorFilter(ColorMatrixColorFilter(cm))
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        return ret
    }

    fun imageProcessing(bitmap: Bitmap): Bitmap {
        var mat = Mat()
        Utils.bitmapToMat(bitmap,mat)
        Imgproc.cvtColor(mat,mat, Imgproc.COLOR_BGR2GRAY)
        if(isCamScanned){

            Imgproc.adaptiveThreshold(
                mat, mat, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 21, 12.0
            )
//            threshold(mat,mat,0.0,255.0,Imgproc.THRESH_OTSU)
        }
        else
            Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_OTSU)
        Utils.matToBitmap(mat,bitmap)
        return bitmap
    }
}
