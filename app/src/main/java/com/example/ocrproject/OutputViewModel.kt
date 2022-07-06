package com.example.ocrproject

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.googlecode.tesseract.android.TessBaseAPI
import com.googlecode.tesseract.android.TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED
import com.googlecode.tesseract.android.TessBaseAPI.OEM_TESSERACT_ONLY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
//import com.google.android.gms.tasks.Task
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.text.Text
//import com.google.mlkit.vision.text.TextRecognition
//import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception

class OutputViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private lateinit var tessBaseAPI:TessBaseAPI
    lateinit var dataPath:String
    lateinit var bitmap: Bitmap
    lateinit var docType:String
    private var txt:String = ""
    private val _outputText = MutableLiveData<String>()
    val outputText: LiveData<String>
        get() = _outputText
    var hashmap = HashMap<String,String>()
    val stringToLine = HashMap<String,Int>()
    val lineToString = HashMap<Int,String>()


    init {
        hashmap["Name"] = ""
        hashmap["Email"] = ""
        hashmap["Telephone No"] = ""
        hashmap["Mobile No"] = ""
        hashmap["Policy No"] = ""
        hashmap["Previous Policy No"] = ""
        hashmap["Vehicle Registration No"] = ""
        hashmap["Vehicle Registation Date"] = ""
        hashmap["E-Policy No"] = ""
        hashmap["Covernote No"] = ""
    }
    fun startModel(){
        viewModelScope.launch(Dispatchers.Default) {
            txt = getText()
            val ans = extractInfo()
            withContext(Dispatchers.Main) {
              _outputText.value = ans
            }

        }
    }

    private fun extractInfo():String{
        val n = txt.length
        var wd = ""
        var i = 0
        var j = 1
        while(i < n){
            while(i < n && !txt[i].equals('\n'))
                wd += txt[i++]
//            println(j.toString()+": "+wd+"\n")
            stringToLine[wd] = j
            lineToString[j] = wd
            wd = ""
            ++i; ++j
        }
        return processText()
    }

    private fun processText():String{
        Log.i("Doc Type: ",docType)
        when(docType){
            "ICICI"-> {
                val ob = ICICI(stringToLine,lineToString)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
            "RELIANCE"->{
                val ob = Reliance(stringToLine)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
            "Bajaj"->{
                val ob = Bajaj(stringToLine)
                ob.lineMatching()
                hashmap = ob.hashmap
            }
        }
        return printData()
    }

    private fun printData():String{
        var wd = ""
        for(keys in hashmap.keys)
            wd += keys+": "+hashmap[keys]+"\n"
       return wd
    }

    private fun getText(): String{
        try {
            tessBaseAPI = TessBaseAPI()
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, e.message!!)
        }
        var begin = System.currentTimeMillis()
        tessBaseAPI.init(dataPath, "eng")
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST,"-%*&#!@/,.:' \"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")
//        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD)
//        tessBaseAPI.words.getBox()
        tessBaseAPI.setImage(bitmap)
        var retStr: String? = "No result"
        try {
            retStr = tessBaseAPI.utF8Text
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, e.message!!)
        }
        var end = System.currentTimeMillis()
        println("Time Taken: ${end-begin}")
        tessBaseAPI.recycle()
        retStr ?: return "No result"
        return retStr
    }






}