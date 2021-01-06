package ru.garretech.readmanganew.ui.mangaInfo

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.garretech.readmanganew.DisposableManager
import ru.garretech.readmanganew.R
import ru.garretech.readmanganew.ui.reader.MangaReaderActivity
import ru.garretech.readmanganew.adapters.ExpandableItemAdapter
import ru.garretech.readmanganew.fragments.ProgressBottomSheet
import ru.garretech.readmanganew.interfaces.OnExpandableItemClickListener
import ru.garretech.readmanganew.models.Chapter
import ru.garretech.readmanganew.models.Manga


class MangaEpisodesFragment : Fragment(), OnExpandableItemClickListener {

    val TAG = this::class.java.simpleName

    private lateinit var episodesAdapter: ExpandableItemAdapter
    var currentManga: Manga? = null

    private lateinit var rootView: View

    private val recyclerView: RecyclerView by lazy { rootView.findViewById<RecyclerView>(R.id.sourcesRecyclerView) }
    private val sourcesProgress: ProgressBar by lazy { rootView.findViewById<ProgressBar>(R.id.sourcesProgress) }
    private val goToLastViewedChapterButton: FloatingActionButton by lazy {
        rootView.findViewById<FloatingActionButton>(
            R.id.goToLastViewedChapterButton
        )
    }

    private lateinit var viewModel: MangaEpisodesViewModel

    private lateinit var progressBottomSheet: ProgressBottomSheet

    var scrollingObserver: RecyclerView.AdapterDataObserver? = null

    private var expandedIndex: Int = -1


    /*  * Переходим по ссылке http://readmanga.me/tower_of_god/vol3/6
    * Считываем количество страниц из элемента с классом pages-count
    * Берем первое фото из div с id=fotocontext. Содержимое аттрибута src из тега img
    * http://e5.mangas.rocks/auto/30/35/40/TowerOfGod_s3_ch06_p01_SIU_Gemini.jpg_res.jpg?t=1556875730&u=0&h=i1nwNAGZO2AF_mAe3BzlHQ
    * Подставляем вместо p01 номера с 1 по количество страниц. Полученный массив ссылок и будет текущий эпизоп манги
    *
    * */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MangaEpisodesViewModel::class.java]

        episodesAdapter = ExpandableItemAdapter(viewModel, ArrayList())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_manga_sources, container, false)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = episodesAdapter

        showProgressBar()


        if (savedInstanceState != null) {
            val url = savedInstanceState.getString(URL_MANGA)
            viewModel.getMangaFromDatabase(url!!) {
                startLoading()
            }
        } else {
            if (viewModel.currentManga == null && currentManga != null)
                viewModel.currentManga = currentManga

            startLoading()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mDivider = context?.getDrawable(R.drawable.line_divider)
            val mDividerItemDecoration = CustomDivider(mDivider!!, 10, 10)
            recyclerView.addItemDecoration(mDividerItemDecoration)
        }


        goToLastViewedChapterButton.setOnClickListener {
            if (episodesAdapter.data.size != 0) {
                val lastWatchedChapterMap = viewModel.historyProvider.getLastWatchedChapter()!!

                //TODO("Найти по выбранному номеру тома её индекс в списке")
                val expandableVolumeIndex = episodesAdapter.getVolumeIndex(lastWatchedChapterMap.keys.first())

                expandableVolumeIndex?.let {
                    episodesAdapter.expand(it, false, true)

                    episodesAdapter.notifyDataSetChanged()

                    val smoothScroller = object : LinearSmoothScroller(context) {
                            override fun getVerticalSnapPreference() =
                                LinearSmoothScroller.SNAP_TO_START
                    }

                    val expandedIndex =
                        it + viewModel.getIndexOfChapterInAdapter(
                            it,
                            lastWatchedChapterMap.values.first()
                        ) + 1

                    if (expandedIndex != -1) {
                        smoothScroller.targetPosition = expandedIndex
                        recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
                    }
                }

            }
        }

        return rootView
    }

    override fun onChapterClick(item: Chapter) {

        val intent = Intent(activity, MangaReaderActivity::class.java)
        intent.putExtra("selectedChapterIndex", item.chapterNumber)
        intent.putExtra("chapterArray", viewModel.chapterJsonArray.toString())
        intent.putExtra("mangaURL", viewModel.currentManga?.url)

        startActivityForResult(intent,
            MANGA_VIEWER_INTENT
        )

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MANGA, viewModel.currentManga?.url)
    }

    private fun startLoading() {
        DisposableManager.add(viewModel.getChaptersList()
            .flatMap {
                viewModel.getHistory()
            }
            .subscribe({
                if (it.history.chapters != null && it.history.chapters!!.size != 0) {
                    showHistoryButton()
                }

                episodesAdapter.onExpandableItemClickListener = this
                updateEpisodeAdapter(viewModel.adapterList)
                dismissProgressBar()
            }, {
                Log.e("Chapter observer", "Error getting chapter list", it)
            })
        )

        //setAdapterRangeChangeListener()
    }

    private fun showConnectionError() {
        if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
            progressBottomSheet.dismissAllowingStateLoss()
        Toast.makeText(context, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    private fun hasConnection(): Boolean {
        val cm = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun updateEpisodeAdapter(episodeList: List<MultiItemEntity>) {
        episodesAdapter.addData(episodeList)
    }

    private fun showProgressBar() {
        sourcesProgress.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        sourcesProgress.visibility = View.GONE
    }

    private fun showHistoryButton() {
        goToLastViewedChapterButton.visibility = View.VISIBLE
    }

    private fun hideHistoryButton() {
        goToLastViewedChapterButton.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            MANGA_VIEWER_INTENT -> {
                viewModel.getHistory().subscribe({

                    if (it.history.chapters != null && it.history.chapters!!.size != 0) {
                        showHistoryButton()
                    }

                    episodesAdapter.notifyDataSetChanged()
                }, {
                    Log.e(TAG, "Ошибка при получении истории", it)
                })
            }
        }
    }

    companion object {

        const val URL_MANGA = "manga_url"
        const val MANGA_VIEWER_INTENT = 5

        fun newInstance(manga: Manga) = MangaEpisodesFragment().also {
            it.currentManga = manga
        }
    }


    internal inner class CustomDivider(
        val mDivider: Drawable,
        val topOffset: Int,
        val bottomOffset: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.top = topOffset
            outRect.bottom = bottomOffset
        }
    }
}// Required empty public constructor
