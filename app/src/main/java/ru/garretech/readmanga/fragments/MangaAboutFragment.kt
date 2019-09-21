package ru.garretech.readmanga.fragments

//import android.arch.persistence.room.PrimaryKey
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.yandex.mobile.ads.*


import org.json.JSONException
import ru.garretech.readmanga.R
import ru.garretech.readmanga.Settings
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.viewmodels.MangaAboutFragmentViewModel
import java.lang.StringBuilder


class MangaAboutFragment : androidx.fragment.app.Fragment() {


    private var currentManga: Manga? = null
    private lateinit var viewModel : MangaAboutFragmentViewModel
    private lateinit var rootView : View

    val mAdMobView: AdView by lazy { AdView(context!!) }
    private var mAdRequest: AdRequest? = null

    private val mangaTitleTextView : TextView by lazy { rootView.findViewById<TextView>(R.id.mangaTitleText) }
    private val mangaAgeView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_age_text) }
    private val mangaGenresView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_genres_text) }
    private val mangaProductionCountryView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_production_country_text) }
    private val mangaChaptersNumberView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_chapters_number_text) }
    private val mangaDurationView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_duration_text) }
    private val imageView : ImageView by lazy { rootView.findViewById<ImageView>(R.id.manga_image_about) }
    private val mangaDescriptionView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_description_text) }

    private val pageLayout : LinearLayout by lazy { rootView.findViewById<LinearLayout>(R.id.mangaInfoScrollContent) }

    private val progressCircle : ProgressBar by lazy { rootView.findViewById<ProgressBar>(R.id.mangaInfoProgressCircle) }

    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {}

        override fun onAdClosed() {}

        override fun onAdLeftApplication() {}

        override fun onAdLoaded() { mAdMobView.visibility = View.VISIBLE }

        override fun onAdOpened() {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MangaAboutFragmentViewModel::class.java)

        viewModel.currentManga = currentManga
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_movie_info, container, false)

        showProgressBar()
        if (savedInstanceState != null) {
            val url = savedInstanceState.getString(URL_MANGA)
            viewModel.getMangaFromDatabase(url!!).subscribe { movie ->
                startLoading()
            }
        } else {
            if (viewModel.currentManga == null && currentManga != null)
                viewModel.currentManga = currentManga

            startLoading()
        }

        return rootView
    }

    private fun startLoading() {
        var genresString = StringBuilder()

        for (genre in viewModel.currentManga?.genres ?: ArrayList())
            genresString.append("$genre, ")

        if (genresString.length != 0)
            mangaGenresView.text = getString(R.string.genres_description) + " " + genresString.substring(0,genresString.length - 2)


        mangaTitleTextView.text = viewModel.currentManga?.title
        mangaProductionCountryView.text = getString(R.string.production_country_description)  + " " + viewModel.currentManga?.productionCountry
        mangaChaptersNumberView.text = viewModel.currentManga?.chaptersNumber
        mangaDurationView.text = viewModel.currentManga?.duration
        mangaAgeView.text = getString(R.string.age_description)  + " " + viewModel.currentManga?.productionYear
        mangaDescriptionView.text = viewModel.currentManga?.description


        if (viewModel.currentManga?.mangaImageURL != null && context != null)
            Glide
                .with(context!!)
                .load(viewModel.currentManga?.mangaImageURL!!)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                //.placeholder(R.drawable.loading_spinner)
                .into(imageView)

        initAdMobView()
        
        dismissProgressBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MANGA,viewModel.currentManga?.url)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshBannerAd()
    }

    private fun showProgressBar() {
        progressCircle?.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        progressCircle?.visibility = View.GONE
    }

    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.BLOCK_ID1
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        pageLayout.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdMobView.loadAd(mAdRequest)
    }

    override fun onDestroy() {
        mAdMobView.destroy()
        super.onDestroy()
    }


    companion object {

        const val URL_MANGA = "manga_url"

        @Throws(JSONException::class)
        fun newInstance(manga: Manga) = MangaAboutFragment().also {
            it.currentManga = manga
        }
    }
}


