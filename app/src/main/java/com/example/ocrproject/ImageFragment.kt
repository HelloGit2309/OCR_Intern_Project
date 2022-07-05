package com.example.ocrproject

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.example.ocrproject.databinding.FragmentImageBinding
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY
import org.opencv.imgproc.Imgproc.threshold


class ImageFragment : Fragment() {

    companion object {
        fun newInstance() = ImageFragment()
    }

    private lateinit var viewModel: ImageViewModel
    private lateinit var binding: FragmentImageBinding
    private var docType = ""
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            imageHandler(uriContent)
        } else {
            // an error occurred
            fragmentManager?.popBackStack()
        }
    }
    private var isCamScanned = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_image,container,false)

        return binding.root
    }
    private lateinit var safeContext:Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

        val result = arguments?.getString("imageUri") as String
        docType = arguments?.getString("docType") as String
        startCrop(Uri.parse(result))

    }

    private fun startCrop(imageUri: Uri) {

        // start cropping activity for pre-acquired image saved on the device and customize settings
        cropImage.launch(
            options(uri = imageUri) {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
            }
        )
    }

    private fun imageHandler(imageUri: Uri?){
        imageUri ?: return
        var bitmap = MediaStore.Images.Media.getBitmap(this.requireContext().contentResolver, imageUri) as Bitmap
        val frag = getCallerFragment().toString()
        Log.d("FRAG VAL",frag)
        if(frag == "camFrag") {
            viewModel.isCamScanned = true
            bitmap = viewModel.changeBitmapContrastBrightness(bitmap,0.9f,60f)
        }
        bitmap = viewModel.imageProcessing(bitmap)
        binding.myImg.setImageBitmap(bitmap)
        binding.okButton.setOnClickListener{
            Toast.makeText(activity,"ML Model Loading",Toast.LENGTH_LONG).show()
            val outputFrag:Fragment = OutputFragment.newInstance()
            val bundle = Bundle()
            bundle.putParcelable("imageBitmap",bitmap)
            bundle.putString("docType",docType)
            outputFrag.setArguments(bundle)
            activity?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(R.id.myContainer, outputFrag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
    private fun getCallerFragment(): String? {
        val count = fragmentManager?.backStackEntryCount
        count ?: return null
        fragmentManager ?: return null
        return fragmentManager!!.getBackStackEntryAt(count - 1).getName()
    }



}