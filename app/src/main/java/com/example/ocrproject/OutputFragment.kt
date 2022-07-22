package com.example.ocrproject

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ocrproject.databinding.FragmentOutputBinding
import com.google.android.material.textfield.TextInputLayout
import dialog
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class OutputFragment : Fragment() {

    companion object {
        fun newInstance() = OutputFragment()
    }

    private lateinit var viewModel: OutputViewModel
    private lateinit var binding: FragmentOutputBinding
    private lateinit var safeContext: Context

    val TESS_DATA: String = "/tessdata"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_output, container, false)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OutputViewModel::class.java)
        val bitmap = arguments?.getParcelable("imageBitmap") as Bitmap?
        viewModel.dataPath =
            activity?.let { it.getExternalFilesDir("/")?.getPath().toString() } + "/"
        viewModel.bitmap = bitmap as Bitmap
        viewModel.docType = arguments?.getString("docType") as String

        prepareTessData()
        val ob = dialog(activity)
        viewModel.startModel(ob)

        viewModel.outputText.observe(viewLifecycleOwner, Observer { output ->
            renderTextFields(output)
        })

    }

    private fun renderTextFields(hashmap: HashMap<String, String>) {
        hashmap.forEach {
            val textView = layoutInflater.inflate(R.layout.edit_text, null) as TextInputLayout
            textView.hint = it.key
            textView.editText?.setText(it.value)
            binding.layoutId.addView(textView)

        }
    }

    private fun prepareTessData() {
        try {
            val dir: File = activity?.getExternalFilesDir(TESS_DATA) as File
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    Toast.makeText(
                        activity,
                        "The folder " + dir.path + "was not created",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            val fileName = "eng.traineddata"
            val pathToDataFile = "$dir/$fileName"
            if (!File(pathToDataFile).exists()) {
                val `in`: InputStream = activity?.getAssets()?.open(fileName) as InputStream
                val out: OutputStream = FileOutputStream(pathToDataFile)
                `in`.copyTo(out)
                `in`.close()
                out.close()
            }

        } catch (e: java.lang.Exception) {
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.stringToLine.clear()
        viewModel.lineToString.clear()
    }


}