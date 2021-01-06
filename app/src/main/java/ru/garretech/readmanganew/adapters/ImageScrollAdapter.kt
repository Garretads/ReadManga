package ru.garretech.readmanganew.adapters

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.viewpager.widget.PagerAdapter
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.view.BigImageView
import ru.garretech.readmanganew.R
import ru.garretech.readmanganew.interfaces.OnViewPagerClickListener


class ImageScrollAdapter(private val mContext: Context, private val mImageList: ArrayList<String>) :
    PagerAdapter() {
    lateinit var onViewPagerClickListener: OnViewPagerClickListener

    init {
        mImageList.forEach {
            BigImageViewer.prefetch(Uri.parse(it))
        }
    }

    fun setCustomOnClickListener(listener: OnViewPagerClickListener) {
        onViewPagerClickListener = listener
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return mImageList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        synchronized(ImageScrollAdapter::class) {

            // if (container.getChildAt(position) == null) {

            val imageView = BigImageView(mContext)
            imageView.setFailureImage(getDrawable(mContext, R.drawable.broken_image))
            imageView.setTapToRetry(true)
            imageView.setOptimizeDisplay(true)
            imageView.setProgressIndicator(ProgressPieIndicator())


            /*if (position > container.childCount) {
                for (index in container.childCount until mImageList.size) {
                    val imageView = BigImageView(mContext)
                    imageView.setFailureImage(getDrawable(mContext,R.drawable.broken_image))
                    imageView.setTapToRetry(true)
                    imageView.setOptimizeDisplay(true)
                    imageView.setProgressIndicator(ProgressPieIndicator())
                    imageView.showImage(Uri.parse(mImageList.get(index)))

                    container.addView(imageView, index)
                    imageView.setOnClickListener {
                        onViewPagerClickListener.onClick()
                    }
                }
            } else {*/

            val imageUrl = mImageList.get(position)

            imageView.showImage(Uri.parse(mImageList.get(position).toString()))
            container.addView(imageView)

            imageView.setOnClickListener {
                onViewPagerClickListener.onClick()
            }

            //}
            return imageView
            /* } else {
                 return container.getChildAt(position)
             }*/
        }
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as BigImageView)
    }
}