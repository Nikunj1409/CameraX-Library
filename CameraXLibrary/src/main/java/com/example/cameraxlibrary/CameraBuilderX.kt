package com.example.cameraxlibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log


class CameraBuilderX(
    private val callBack: CallBack,
    private val context: Context
) {
    private var brodCast = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            val list = p1?.getStringArrayListExtra("list")
            Log.e("TAG", "onReceive: ${list.toString()}")

            callBack.doImageListURL(list)
            context.unregisterReceiver(this)
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(brodCast, IntentFilter("value"), RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(brodCast, IntentFilter("value"))
        }

    }

    companion object {
        const val IMAGE = 1
        const val VIDEO = 2
        const val CAMERA_SHOOT = 3
        const val VIDEO_SHOOT = 4
    }

    private var selection = 0
    private var videoTime = 0
    private var type = 0

    fun videoTime(value: Int): CameraBuilderX {
        videoTime = value;
        return this
    }

    fun selection(value: Int): CameraBuilderX {
        selection = value;
        return this
    }

    fun setType(type: Int): CameraBuilderX {
        this.type = type
        return this

    }


    fun build() {
        when (type) {
            IMAGE -> {
                val i = Intent(context, GalleryActivity::class.java)
                i.putExtra("value", IMAGE)
                i.putExtra("selectNumberValue", selection)
                context.startActivity(i)
            }

            VIDEO -> {
                val i = Intent(context, GalleryActivity::class.java)
                i.putExtra("value", VIDEO)
                i.putExtra("selectNumberValue", selection)
                context.startActivity(i)
            }

            CAMERA_SHOOT -> {
                val i = Intent(context, CameraActivity::class.java)
                i.putExtra("value", "camera")
                context.startActivity(i)
            }

            VIDEO_SHOOT -> {
                val i = Intent(context, CameraActivity::class.java)
                i.putExtra("value", "video")
                i.putExtra("second", videoTime)
                context.startActivity(i)
            }

            else -> {}
        }
    }


    interface CallBack {
        fun doImageListURL(list: List<String>?) {

        }
    }
}