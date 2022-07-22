package com.example.ocrproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TitleViewModel : ViewModel() {

    private val _scan = MutableLiveData<Boolean>()
    val scan: LiveData<Boolean>
        get() = _scan
    private val _upload = MutableLiveData<Boolean>()
    val upload: LiveData<Boolean>
        get() = _upload

    init {
        _scan.value = false
        _upload.value = false
    }

    fun onScanDoc() {
        _scan.value = true
    }

    fun onAfterScanPress() {
        _scan.value = false
    }

    fun onUploadDoc() {
        _upload.value = true
    }

    fun onAfterUploadPress() {
        _upload.value = false
    }

}