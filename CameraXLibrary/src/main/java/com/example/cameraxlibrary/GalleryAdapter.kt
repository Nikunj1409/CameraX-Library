package com.example.cameraxlibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

open class GalleryAdapter(private val context: Context) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private var arrayOfImage: ArrayList<GalleryImage>? = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)
        Glide.with(context)
            .load(image?.path)
            .into(holder.imageView)

        if (image?.isSelected == true){
            holder.imgSelect.visible()
            holder.view.visible()
        }else{
            holder.imgSelect.gone()
            holder.view.gone()
        }

        if (image?.type == "Video"){
            holder.imgPlay.visible()
        }else{
            holder.imgPlay.gone()
        }
    }

    override fun getItemCount(): Int {
        return arrayOfImage?.size ?: 0
    }

    fun updateList(list: List<GalleryImage>) {
        arrayOfImage?.clear()
        arrayOfImage?.addAll(list)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): GalleryImage? {
        return arrayOfImage?.get(position)
    }
    fun getList(): ArrayList<GalleryImage>? {
        return arrayOfImage
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val imgSelect: ImageView = itemView.findViewById(R.id.imgSelect)
        val imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        val view: View = itemView.findViewById(R.id.view)

        init {
            imageView.setOnClickListener {
                val model = getItem(adapterPosition)
                itemClick(adapterPosition,model)
            }
            imgPlay.setOnClickListener {
                imageView.performClick()
            }
        }
    }

    open fun itemClick(position: Int, model: GalleryImage?) {

    }
    open fun itemPlayClick(position: Int, model: GalleryImage?) {

    }
}
