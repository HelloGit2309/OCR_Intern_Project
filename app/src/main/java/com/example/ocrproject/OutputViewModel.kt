package com.example.ocrproject

import android.content.ContentValues
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.googlecode.tesseract.android.TessBaseAPI
import dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutputViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private lateinit var tessBaseAPI: TessBaseAPI
    lateinit var dataPath: String
    lateinit var bitmap: Bitmap
    lateinit var docType: String
    private var txt: String = ""
    var outputText = MutableLiveData<HashMap<String, String>>()
        private set

    var hashmap = HashMap<String, String>()
    val stringToLine = HashMap<String, Int>()
    val lineToString = HashMap<Int, String>()

    fun startModel(ob: dialog) {

        ob.startLoadingdialog()
        viewModelScope.launch(Dispatchers.Default) {

            txt = getText()
            extractInfo()

            withContext(Dispatchers.Main) {
                ob.dismissdialog()
                outputText.value = hashmap
            }

        }
    }

    private fun extractInfo() {
        val n = txt.length
        var wd = ""
        var index = 0
        var lineNumber = 1
        while (index < n) {
            while (index < n && !txt[index].equals('\n'))
                wd += txt[index++]
            stringToLine[wd] = lineNumber
            lineToString[lineNumber] = wd
            wd = ""
            ++index; ++lineNumber
        }
        processText()
    }

    private fun processText() {
        when (docType) {
            "ICICI" -> {
                val ob = ICICI(stringToLine, lineToString)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
            "RELIANCE" -> {
                val ob = Reliance(stringToLine)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
            "Bajaj" -> {
                val ob = Bajaj(stringToLine)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
        }
    }


    private fun getText(): String {
        try {
            tessBaseAPI = TessBaseAPI()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, e.message!!)
        }
        val begin = System.currentTimeMillis()
        // initialising Tess with training model
        tessBaseAPI.init(dataPath, "eng")
        // setting allowed characters to detect
        tessBaseAPI.setVariable(
            TessBaseAPI.VAR_CHAR_WHITELIST,
            "-%*&#!@/,.:' \"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        )
        // passing input
        tessBaseAPI.setImage(bitmap)
        var retStr: String? = "No result"
        try {
            // storing OCR result
            retStr = tessBaseAPI.utF8Text
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, e.message!!)
        }
        val end = System.currentTimeMillis()
        println("Time Taken: ${end - begin}")
        tessBaseAPI.recycle()
        retStr ?: return "No result"
        return retStr
    }


}