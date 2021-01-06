package ru.garretech.readmanganew.ui.reader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_manga_reader.*
import org.json.JSONArray
import org.json.JSONObject
import ru.garretech.readmanganew.DisposableManager
import ru.garretech.readmanganew.interfaces.OnViewPagerClickListener
import ru.garretech.readmanganew.R
import ru.garretech.readmanganew.adapters.ImageScrollAdapter
import ru.garretech.readmanganew.providers.SiteContentProvider

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MangaReaderActivity : AppCompatActivity(), OnViewPagerClickListener, PagePickerFragment.OnNumberPickedListener {

    val chapterJsonArray : JSONArray by lazy { JSONArray(intent.getStringExtra("chapterArray")) }
    val mangaURL : String by lazy { intent.getStringExtra("mangaURL") ?: "" }
    var selectedChapterIndex = 0
    lateinit var mMenu : Menu
    lateinit var adapter: ImageScrollAdapter
    //lateinit var adapterNew : ImageRecyclerAdapter

    lateinit var viewModel : MangaReaderActivityViewModel


    override fun onNumberPicked(pageIndex: Int) {
        mangaContentView.setCurrentItem(pageIndex-1,true)
    }

    override fun onClick() {
        toggle()
    }

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {

        mangaContentView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga_reader)

        viewModel = ViewModelProviders.of(this).get(MangaReaderActivityViewModel::class.java)

        setSupportActionBar(mangaReaderToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //val imageListString = intent.getStringExtra("imageList")
        //val chapterName = intent.getStringExtra("chapterName")
        selectedChapterIndex = intent.getIntExtra("selectedChapterIndex",0)

        val selectedChapter = chapterJsonArray[selectedChapterIndex] as JSONObject
        val chapterName = selectedChapter.getString("volumeNumber") + "-" +
                        selectedChapter.getString("chapterNumber")  + " " +
                        selectedChapter.getString("chapterName")

        title = chapterName

        mVisible = true

        //adapterNew = ImageRecyclerAdapter()

        //mangaContentView.adapter = adapterNew

        DisposableManager.add(viewModel.prepareHistory(mangaURL).subscribe({
            prepareImageSet(mangaURL,selectedChapter.getString("link"))
        },{
            Log.e("MangaReaderActivity","Ошибка при получении истории",it)
        }))

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        delayedHide(100)
    }



    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        mangaContentView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {

        private const val AUTO_HIDE = true

        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        private const val UI_ANIMATION_DELAY = 300
    }

    private fun prepareImageSet(mangaURL : String, path : String) {
        showProgressBar()
        mangaContentView.offscreenPageLimit = 4
        mangaContentView.removeAllViews()
        DisposableManager.add(getPhotosRequestSingle(mangaURL + path).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ jsonArray ->

                var imageList = ArrayList<String>()

                for (index in 0 until jsonArray.length()) {
                    val jsonTemp = jsonArray.getJSONArray(index)

                    val link = jsonTemp.get(0).toString() + jsonTemp.get(2).toString()

                    imageList.add(link)
                }


                //adapterNew.addAll(imageList)
                adapter = ImageScrollAdapter(this,imageList)

                pageCount.text = imageList.size.toString()
                updateCurrentPageText(1)
                adapter.setCustomOnClickListener(this)
                mangaContentView.adapter = adapter
                mangaContentView.invalidate()
                //mangaContentView.invalidate()

               /* mangaContentView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        updateCurrentPageText(position+1)
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    }
                })*/
                mangaContentView.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {

                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        updateCurrentPageText(position+1)
                    }

                    override fun onPageSelected(position: Int) {

                    }
                })

                pageSelectorLayout.setOnClickListener {
                    val pagePicker = PagePickerFragment.newInstance(mangaContentView.currentItem + 1, imageList.size)
                    pagePicker.view?.setBackgroundResource(android.R.color.transparent)
                    pagePicker.show(supportFragmentManager,"pagePicker")
                }

                dismissProgressBar()
            }, { error ->
                Log.e("MangaReaderActivity", "Ошибка получения списка картинок", error)
            }))
    }

    private fun getPhotosRequestSingle(url: String) : Single<JSONArray> {
        return Single.create { observer ->
            val jsonArray = SiteContentProvider.getMangaImageList(url)
            observer.onSuccess(jsonArray)
        }
    }

    private fun showProgressBar() {
        readerProgress.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        readerProgress.visibility = View.GONE
    }

    fun updateCurrentPageText(value : Int) {
        currentPageText.text = value.toString()
    }

    fun previousChapter(view: View) {
        if (selectedChapterIndex != 0) {
            showProgressBar()
            selectedChapterIndex--
            val newChapter = chapterJsonArray[selectedChapterIndex] as JSONObject

            val chapterName = newChapter.getString("volumeNumber") + "-" +
                    newChapter.getString("chapterNumber")  + " " +
                    newChapter.getString("chapterName")

            viewModel.historyProvider.addChapter(newChapter.getString("volumeNumber").toInt(),newChapter.getString("chapterNumber").toInt())

            viewModel.addToHistory {
                title = chapterName
                prepareImageSet(mangaURL, newChapter.getString("link"))
            }

        }
    }

    fun nextChapter(view: View) {
        if (selectedChapterIndex < chapterJsonArray.length()-1) {
            showProgressBar()
            selectedChapterIndex++
            val newChapter = chapterJsonArray[selectedChapterIndex] as JSONObject

            val chapterName = newChapter.getString("volumeNumber") + "-" +
                    newChapter.getString("chapterNumber")  + " " +
                    newChapter.getString("chapterName")

            viewModel.historyProvider.addChapter(newChapter.getString("volumeNumber").toInt(),newChapter.getString("chapterNumber").toInt())

            viewModel.addToHistory {
                title = chapterName
                prepareImageSet(mangaURL, newChapter.getString("link"))
            }

        }
    }


    fun slidePrevious(view: View) {
        mangaContentView.setCurrentItem(mangaContentView.currentItem - 1, true)
        updateCurrentPageText(mangaContentView.currentItem + 1)
    }

    fun slideNext(view: View) {
        mangaContentView.setCurrentItem(mangaContentView.currentItem + 1, true)
        updateCurrentPageText(mangaContentView.currentItem + 1)
    }
}
