package ru.garretech.readmanga.adapters

import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import ru.garretech.readmanga.R
import ru.garretech.readmanga.interfaces.OnExpandableItemClickListener
import ru.garretech.readmanga.models.Chapter
import ru.garretech.readmanga.models.Volume
import ru.garretech.readmanga.viewmodels.MangaEpisodesFragmentViewModel


class ExpandableItemAdapter(
    private val viewModel: MangaEpisodesFragmentViewModel,
    data: List<MultiItemEntity>
) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(data) {
    var onExpandableItemClickListener: OnExpandableItemClickListener? = null

    var selectedVolume: Int = -1

    init {
        addItemType(Volume.TYPE, R.layout.item_expandable_volume)
        addItemType(Chapter.TYPE, R.layout.item_expandable_chapter)
    }


    fun setOnChapterClickListener(listener: OnExpandableItemClickListener) {
        onExpandableItemClickListener = listener
    }


    override fun convert(helper: BaseViewHolder?, item: MultiItemEntity?) {
        when (helper?.itemViewType) {
            Volume.TYPE -> {
                val volume = item as Volume
                helper.setText(
                    R.id.volumeNameText, "Том ${volume.volumeNumber}"
                )

                val watchedVolumeIndexes = viewModel.getWatchedVolumeIndexes()

                if (watchedVolumeIndexes.contains(volume.volumeNumber))
                    flagWatchedVolume(helper)
                else
                    unflagWatchedVolume(helper)


                helper.itemView.setOnClickListener {
                    val pos = helper.adapterPosition
                    selectedVolume = volume.volumeNumber

                    if (item.isExpanded) {
                        collapse(pos)
                    } else {
                        expand(pos)
                    }
                }
            }
            Chapter.TYPE -> {
                val chapter = item as Chapter
                helper.setText(R.id.chapterNameText, chapter.chapterTitleName)

                val watchedChapters = viewModel.getWatchedChaptersInVolume(chapter.volumeNumber)

                if (watchedChapters.contains(chapter.chapterNumber))
                    flagWatchedChapter(helper)
                else
                    unflagWatchedChapter(helper)

                helper.itemView.setOnClickListener {

                    viewModel.historyProvider.addChapter(
                        chapter.volumeNumber,
                        chapter.chapterNumber
                    )
                    flagWatchedChapter(helper)
                    notifyDataSetChanged()

                    viewModel.addToHistory().subscribe {
                        onExpandableItemClickListener?.onChapterClick(chapter)
                    }

                }
            }
        }
    }


    private fun flagWatchedVolume(helper: BaseViewHolder?) {
        helper?.setVisible(R.id.watchedVolumeImageView, true)
    }

    private fun unflagWatchedVolume(helper: BaseViewHolder?) {
        helper?.setVisible(R.id.watchedVolumeImageView, false)
    }

    private fun flagWatchedChapter(helper: BaseViewHolder?) {
        helper?.setTextColor(
            R.id.chapterNameText,
            ContextCompat.getColor(helper.itemView.context!!, R.color.watched_source)
        )
    }

    private fun unflagWatchedChapter(helper: BaseViewHolder?) {
        helper?.setTextColor(
            R.id.chapterNameText,
            ContextCompat.getColor(helper.itemView.context!!, android.R.color.secondary_text_dark)
        )
    }


}