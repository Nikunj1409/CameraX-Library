package com.example.cameraxlibrary

data class GalleryImage(
    val id: Long? = 0L,
    val path: String,
    var isSelected :Boolean?= false,
    var type :String?= "Image",
)
