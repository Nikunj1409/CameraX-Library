package com.example.cameraxlibrary

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameraxlibrary.databinding.ActivityGalleryBinding
import java.io.File


class GalleryActivity : AppCompatActivity() {
    private val mainBinding: ActivityGalleryBinding by lazy {
        ActivityGalleryBinding.inflate(layoutInflater)
    }
    private val galleryAdapter by lazy {
        object : GalleryAdapter(this) {
            override fun itemClick(position: Int, model: GalleryImage?) {
                super.itemClick(position, model)
                if ((totalSelectedPic ?: 0) <= 1) {
                    val arraylist = arrayListOf<String>()
                    arraylist.add(model?.path ?: "")
                    val i = Intent("value")
                    i.putStringArrayListExtra("list", arraylist)
                    sendBroadcast(i)
                    finish()
                } else {
                    val count = getList()?.filter { it.isSelected == true }?.size
                    if ((count ?: 0) >= totalSelectedPic) {
                        if (model?.isSelected == true) {
                            model.isSelected = false
                            notifyItemChanged(position)
                        } else {
                            Toast.makeText(
                                this@GalleryActivity,
                                "You have selected max pictures",
                                Toast.LENGTH_SHORT
                            )
                            return
                        }
                    } else {
                        if (model?.isSelected == true) {
                            model.isSelected = false
                        } else {
                            model?.isSelected = true
                        }
                        notifyItemChanged(position)
                    }
                    val imageCount = getList()?.filter { it.isSelected == true }?.size
                    if (imageCount == 0) {
                        mainBinding.tvDone.invisible()
                    } else {
                        mainBinding.tvDone.visible()
                    }
                    mainBinding.tvTitle.text = "${imageCount ?: 0}/$totalSelectedPic"
                }

            }
        }
    }
    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
        )
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    var totalSelectedPic = 0
    var galleryValue = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)

        val maxSelectPhotoNumber = intent.getIntExtra("selectNumberValue", 0)
        val getValue = intent.getIntExtra("value", 0)
        if (getValue != null) {
            galleryValue = getValue
        }
        if (maxSelectPhotoNumber != null) {
            totalSelectedPic = maxSelectPhotoNumber
        }

        when (galleryValue) {
            1 -> {
                if ((maxSelectPhotoNumber ?: 0) > 1) {
                    mainBinding.llToolBar.visible()
                    mainBinding.tvTitle.text = "0/${totalSelectedPic}"
                } else {
                    mainBinding.llToolBar.gone()
                }
            }

            2 -> {
                if ((maxSelectPhotoNumber ?: 0) > 1) {
                    mainBinding.llToolBar.visible()
                    mainBinding.tvTitle.text = "0/${totalSelectedPic}"
                } else {
                    mainBinding.llToolBar.gone()
                }
            }

            else -> {}
        }

        mainBinding.recyclerView.adapter = galleryAdapter
        if (checkMultiplePermission()) {
            when (getValue) {
                1 -> {
                    loadGalleryImages()
                }

                2 -> {
                    loadVideoGalleryImages()
                }

                else -> {
                    loadGalleryImages()
                }
            }
        }

        mainBinding.imgBack.setOnClickListener {
            finish()
        }
        mainBinding.tvDone.setOnClickListener {
            val arraylist = arrayListOf<String>()
            galleryAdapter.getList()?.forEach {
                if (it.isSelected == true) {
                    arraylist.add(it.path)
                }
            }
            val i = Intent("value")
            i.putStringArrayListExtra("list", arraylist)
            sendBroadcast(i)
            finish()
        }
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, listPermissionNeeded.toTypedArray(), multiplePermissionId
            )
            return false
        }
        return true
    }

    private fun isDocumentFile(file: File): Boolean {
        val documentExtensions = arrayOf("doc", "docx", "xls", "xlsx", "pdf")
        return documentExtensions.any { file.extension.equals(it, true) }
    }


    private fun loadGalleryImages() {
        val imagesList = mutableListOf<GalleryImage>()
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA),
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val path = it.getString(pathColumn)
                val type = "Image"
                imagesList.add(GalleryImage(id, path, type = type))
            }
        }

        cursor?.close()
        galleryAdapter.updateList(imagesList.reversed())
    }

    private fun loadVideoGalleryImages() {
        val imagesList = mutableListOf<GalleryImage>()
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA),
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val path = it.getString(pathColumn)
                val type = "Video"
                imagesList.add(GalleryImage(id, path, type = type))
            }
        }

        cursor?.close()
        galleryAdapter.updateList(imagesList.reversed())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
//                    loadGalleryImages()
                    when (galleryValue) {
                        1 -> {
                            loadGalleryImages()
                        }

                        2 -> {
                            loadVideoGalleryImages()
                        }

                        else -> {
                            loadGalleryImages()
                        }
                    }
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this, permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this, permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }
}
