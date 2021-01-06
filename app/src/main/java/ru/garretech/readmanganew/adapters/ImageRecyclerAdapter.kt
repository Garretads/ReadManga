package ru.garretech.readmanganew.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.view.BigImageView
import ru.garretech.readmanganew.R

class ImageRecyclerAdapter : RecyclerView.Adapter<ImageRecyclerAdapter.PagerVH>() {

    private var imageList = ArrayList<String>()


    fun addAll(list: ArrayList<String>) {
        imageList = list
        /* for (url in imageList) {
             BigImageViewer.prefetch(Uri.parse(url))
         }*/
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val pagerVH = PagerVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_chapter_image,
                parent,
                false
            )
        )

        pagerVH.chapterImageView = pagerVH.itemView.findViewById(R.id.chapterBigImageView)
        pagerVH.chapterImageView.setProgressIndicator(ProgressPieIndicator())

        return pagerVH
    }

    override fun getItemCount() = imageList.size


    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        holder.chapterImageView.showImage(Uri.parse(imageList.get(position)))
    }


    class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var chapterImageView: BigImageView

    }
}