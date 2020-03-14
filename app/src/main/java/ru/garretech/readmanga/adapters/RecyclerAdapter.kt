package ru.garretech.readmanga.adapters

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import ru.garretech.readmanga.R
import ru.garretech.readmanga.models.Manga

class RecyclerAdapter(layoutResId: Int, data: List<Manga>?) :
    BaseQuickAdapter<Manga, BaseViewHolder>(layoutResId, data) {

    override fun addData(data: Manga) {
        synchronized(this) {
            super.addData(data)
            notifyDataSetChanged()
        }
    }

    fun addAll(movies: Collection<Manga>) {
        data.clear()
        var index = 0
        synchronized(this) {
            for (movie in movies) {
                data.add(movie)
                notifyItemInserted(index)
                index++
            }
            notifyDataSetChanged()
        }
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }


    override fun convert(helper: BaseViewHolder, item: Manga) {
        helper.setText(R.id.mangaTitle, item.title)
        val imageView = helper.itemView.findViewById<ImageView>(R.id.mangaPhoto)


        Glide
            .with(helper.itemView.context!!)
            .load(item.mangaImageURL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .fitCenter()
            //.placeholder(R.drawable.loading_spinner)
            .into(imageView)
    }
}
