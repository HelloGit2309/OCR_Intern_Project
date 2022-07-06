package com.example.ocrproject

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ocrproject.databinding.FragmentTitleBinding
import java.io.File


class TitleFragment : Fragment() {

    // open project
    companion object {
        fun newInstance() = TitleFragment()
    }
    private lateinit var binding: FragmentTitleBinding
    private lateinit var viewModel: TitleViewModel
    private val PICKFILE_REQUEST_CODE = 3
    private lateinit var safeContext:Context
    private lateinit var docType:String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        docType = ""
         binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_title,
            container,
            false
        )
        // get reference to the string array that we just created
        val doc_type = resources.getStringArray(R.array.Document_Type)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(safeContext, R.layout.dropdown_item, doc_type)
        // set adapter to the autocomplete tv to the arrayAdapter
        binding.autoCompleteTextView.setAdapter(arrayAdapter)
        binding.autoCompleteTextView.setOnClickListener{
            binding.autoCompleteTextView.showDropdown(arrayAdapter)
        }

        viewModel = ViewModelProvider(this).get(TitleViewModel::class.java)
        binding.titleViewModel = viewModel

        viewModel.scan.observe(viewLifecycleOwner, Observer{myScan ->
            if(myScan)
                changeToCamFrag()
        })

        viewModel.upload.observe(viewLifecycleOwner,Observer{myUpload ->
            if(myUpload)
            {
                val docType = binding.autoCompleteTextView.text.toString()
                if(docType == "Select Document Type")
                {
                    Toast.makeText(activity,"Select Document Type",Toast.LENGTH_SHORT).show()
                    viewModel.onAfterUploadPress()
                }
                else{
                    viewModel.onAfterUploadPress()
                    onSelectImage()
                }

            }
        })

        return binding.root
    }

    private fun changeToCamFrag(){
        val docType = binding.autoCompleteTextView.text.toString()
        if(docType == "Select Document Type")
        {
            viewModel.onAfterScanPress()
            Toast.makeText(activity,"Select Document Type",Toast.LENGTH_SHORT).show()
        }
        else{
            viewModel.onAfterScanPress()
            val cameraFragment:Fragment = CameraFragment.newInstance()
            val bundle = Bundle()
            bundle.putString("docType",docType)
            cameraFragment.arguments = bundle
            activity?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(R.id.myContainer, cameraFragment)
                    .addToBackStack("titleFrag")
                    .commit()
            }
        }
    }


    fun AutoCompleteTextView.showDropdown(adapter: ArrayAdapter<String>?) {
        if(!TextUtils.isEmpty(this.text.toString())){
            adapter?.filter?.filter(null)
        }
    }

    private fun onCheckStoragePermission():Boolean{
        var a:Boolean = ContextCompat.checkSelfPermission(safeContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        var b:Boolean = ContextCompat.checkSelfPermission(safeContext, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return a&&b
    }

    private fun onSelectImage(){
        if(!onCheckStoragePermission())
        {
            Toast.makeText(activity,"Storage Permission Required",Toast.LENGTH_SHORT)
//            return
        }
        val intent:Intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("*/*");
        startActivityForResult(intent,PICKFILE_REQUEST_CODE)
    }

    private fun getMimeType(context: Context, uri: Uri?): String? {
        uri ?: return ""
        val extension: String?

        //Check uri format to avoid null
        extension = if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            uri.path?.let {
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(it)).toString())
            }

        }
        return extension
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === PICKFILE_REQUEST_CODE && resultCode === Activity.RESULT_OK) {
            data ?: return

            val fileExt = getMimeType(safeContext,data.data) as String
//            val fileExt = uri.substring(uri.lastIndexOf("."))
            Log.i("HElloo",fileExt)
            if(fileExt != "jpg" && fileExt != "jpeg" && fileExt != "png" && fileExt != "pdf")
            {
                Toast.makeText(activity,"File Not Image",Toast.LENGTH_SHORT).show()
                fragmentManager?.popBackStack()
                return
            }
            val imageFrag:Fragment = ImageFragment.newInstance()
            val bundle = Bundle()
            bundle.putString("imageUri",data.data.toString())
            bundle.putString("docType",binding.autoCompleteTextView.text.toString())
            imageFrag.setArguments(bundle)
            activity?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(R.id.myContainer, imageFrag)
                    .addToBackStack("titleFrag")
                    .commit()
            }
        }
    }

}