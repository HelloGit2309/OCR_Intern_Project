package com.example.ocrproject








import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.example.ocrproject.databinding.FragmentCameraBinding
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import kotlin.concurrent.thread


class CameraFragment : Fragment() {


    private lateinit var safeContext: Context
    private var imageCapture: ImageCapture? = null
    private lateinit var binding: FragmentCameraBinding
    private lateinit var viewModel: CameraViewModel
    private var docType = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        safeContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_camera,container,false)
        docType = arguments?.getString("docType") as String
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        if(onCheckCamPermission()){
            Toast.makeText(activity,"Camera Perission Required",Toast.LENGTH_SHORT).show()
            fragmentManager?.popBackStack()
        }

        startCamera()
        binding.imageCapture.setOnClickListener {
            takePhoto()
        }
    }

    private fun onCheckCamPermission(): Boolean {
    return ContextCompat.checkSelfPermission(safeContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
    }


    private var currentPhotoPath:String = ""
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name:File = createImageFile()
         //Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(name)
            .build()


        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(safeContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXApp", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                    // adding Image URI as Bundle
                    val imageFrag:Fragment = ImageFragment.newInstance()
                    val bundle = Bundle()
                    bundle.putString("imageUri",output.savedUri.toString())
                    bundle.putString("docType",docType)
                    imageFrag.setArguments(bundle)
                    activity?.let {
                        it.supportFragmentManager.beginTransaction()
                            .replace(R.id.myContainer, imageFrag)
                            .addToBackStack("camFrag")
                            .commit()
                    }
//                    setFragmentResult("requestKey", bundleOf("bundleKey" to output.savedUri.toString()))
//                    navigateToImage()
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(safeContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,imageCapture)

                // Adding auto focus on CameraX

                binding.viewFinder.afterMeasured {
                    binding.viewFinder.setOnTouchListener { _, event ->
                        return@setOnTouchListener autoFocus(event,camera)
                    }
                }

            } catch(exc: Exception) {
                Log.e("CameraXApp", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(safeContext))
    }

    private fun autoFocus(event: MotionEvent, camera: Camera):Boolean{
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    binding.viewFinder.width.toFloat(), binding.viewFinder.height.toFloat()
                )
                val autoFocusPoint = factory.createPoint(event.x, event.y)
                try {
                    camera.cameraControl.startFocusAndMetering(
                        FocusMeteringAction.Builder(
                            autoFocusPoint,
                            FocusMeteringAction.FLAG_AF
                        ).apply {
                            //focus only when the user tap the preview
                            disableAutoCancel()
                        }.build()
                    )
                } catch (e: CameraInfoUnavailableException) {
                    Log.d("ERROR", "cannot access camera", e)
                }
                return true
            }
            else -> return false // Unhandled event.
        }
    }

    inline fun View.afterMeasured(crossinline block: () -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block()
                    }
                }
            })
        }
    }

    companion object {
        fun newInstance() = CameraFragment()
    }

}
