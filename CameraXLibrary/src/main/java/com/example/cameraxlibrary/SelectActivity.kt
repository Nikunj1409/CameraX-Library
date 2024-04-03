package com.example.cameraxlibrary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.cameraxlibrary.PdfHelper.handlePdfFileSelection
import com.example.cameraxlibrary.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private val mainBinding: ActivitySelectBinding by lazy {
        ActivitySelectBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainBinding.root)


        mainBinding.btnCamera.setOnClickListener {
            CameraBuilderX(object : CameraBuilderX.CallBack {
                override fun doImageListURL(list: List<String>?) {
                    super.doImageListURL(list)
                    Toast.makeText(this@SelectActivity, list.toString(), Toast.LENGTH_SHORT).show()

                }
            }, this)
                .setType(CameraBuilderX.CAMERA_SHOOT)
                .selection(1)
                .build()
        }

        mainBinding.btnVideo.setOnClickListener {
            CameraBuilderX(object : CameraBuilderX.CallBack {
                override fun doImageListURL(list: List<String>?) {
                    super.doImageListURL(list)
                    Toast.makeText(this@SelectActivity, list.toString(), Toast.LENGTH_SHORT).show()

                }
            }, this)
                .setType(CameraBuilderX.VIDEO_SHOOT)
                .selection(1)
                .videoTime(0)
                .build()
        }

        mainBinding.btnThirtyVideo.setOnClickListener {
            CameraBuilderX(object : CameraBuilderX.CallBack {
                override fun doImageListURL(list: List<String>?) {
                    super.doImageListURL(list)
                    Toast.makeText(this@SelectActivity, list.toString(), Toast.LENGTH_SHORT).show()
                }
            }, this)
                .setType(CameraBuilderX.VIDEO_SHOOT)
                .selection(1)
                .videoTime(30)
                .build()
        }
        mainBinding.btnSinglePhoto.setOnClickListener {
            CameraBuilderX(object : CameraBuilderX.CallBack {
                override fun doImageListURL(list: List<String>?) {
                    super.doImageListURL(list)
                    Toast.makeText(this@SelectActivity, list.toString(), Toast.LENGTH_SHORT).show()
                }
            }, this)
                .setType(CameraBuilderX.IMAGE)
                .selection(1)
                .build()
        }
        mainBinding.btnSingleVideo.setOnClickListener {
            CameraBuilderX(object : CameraBuilderX.CallBack {
                override fun doImageListURL(list: List<String>?) {
                    super.doImageListURL(list)
                    Toast.makeText(this@SelectActivity, list.toString(), Toast.LENGTH_SHORT).show()
                }
            }, this)
                .setType(CameraBuilderX.VIDEO)
                .selection(1)
                .build()
        }
        mainBinding.btnFiles.setOnClickListener {
            PdfHelper.selectPdfFile(this, "pdf")
        }
        mainBinding.btnMultiplePhotoSelect.setOnClickListener {
            val dialogFrag = DialogueInfo(object : DialogueInfo.CallBack {
                override fun setData(it: String) {
                    if (!it.isBlank()) {
                        CameraBuilderX(object : CameraBuilderX.CallBack {
                            override fun doImageListURL(list: List<String>?) {
                                super.doImageListURL(list)
                                Log.e("TAG", "doImageListURL: ${list.toString()}")
                                Toast.makeText(
                                    this@SelectActivity,
                                    list.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, this@SelectActivity)
                            .setType(CameraBuilderX.IMAGE)
                            .selection(it.toInt())
                            .build()
                    }
                }
            })
            dialogFrag.show(supportFragmentManager, "")
        }
        mainBinding.btnMultipleVideoSelect.setOnClickListener {
            val dialogFrag = DialogueInfo(object : DialogueInfo.CallBack {
                override fun setData(it: String) {
                    if (!it.isBlank()) {
                        CameraBuilderX(object : CameraBuilderX.CallBack {
                            override fun doImageListURL(list: List<String>?) {
                                super.doImageListURL(list)
                                Toast.makeText(
                                    this@SelectActivity,
                                    list.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }, this@SelectActivity)
                            .setType(CameraBuilderX.VIDEO)
                            .selection(it.toInt())
                            .build()
                    }
                }
            })
            dialogFrag.show(supportFragmentManager, "")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Call handlePdfFileSelection to handle the selected PDF file
        handlePdfFileSelection(this, requestCode, resultCode, data)
    }
}