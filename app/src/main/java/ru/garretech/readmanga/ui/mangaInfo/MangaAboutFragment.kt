package ru.garretech.readmanga.ui.mangaInfo

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.my.target.ads.MyTargetView
import com.yandex.mobile.ads.*
import org.json.JSONException
import ru.garretech.readmanga.R
import ru.garretech.readmanga.Settings
import ru.garretech.readmanga.databinding.FragmentMovieInfoBinding
import ru.garretech.readmanga.models.Manga


class MangaAboutFragment : androidx.fragment.app.Fragment() {

    private var currentManga: Manga? = null
    private lateinit var viewModel: MangaAboutViewModel

    val myTargetAdView: MyTargetView by lazy { MyTargetView(context!!) }
    val mAdMobView: AdView by lazy { AdView(context!!) }

    private var mAdRequest: AdRequest? = null

    private lateinit var binding: FragmentMovieInfoBinding

    private val mangaTitleTextView: TextView get() = binding.mangaTitleText
    private val mangaAgeView: TextView get() =  binding.mangaAgeText
    private val mangaGenresView: TextView get() = binding.mangaGenresText
    private val mangaProductionCountryView: TextView get() = binding.mangaProductionCountryText
    private val mangaChaptersNumberView: TextView get() = binding.mangaChaptersNumberText
    private val mangaDurationView: TextView get() = binding.mangaDurationText
    private val mangaImageAbout: ImageView get() = binding.mangaImageAbout
    private val mangaDescriptionView: TextView get() = binding.mangaDescriptionText
    private val mainContainer: LinearLayout get() = binding.mangaInfoContainer
    private val progressCircle: ProgressBar get() = binding.mangaInfoProgressCircle as ProgressBar

    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {}
        override fun onAdClosed() {}
        override fun onAdLeftApplication() {}
        override fun onAdLoaded() { mAdMobView.visibility = View.VISIBLE }
        override fun onAdOpened() {}
    }

    private val myTargetViewListener = object : MyTargetView.MyTargetViewListener {
        override fun onLoad(p0: MyTargetView) {
            val a = 5
        }

        override fun onClick(p0: MyTargetView) {
            val a = 5
        }

        override fun onNoAd(p0: String, p1: MyTargetView) {
            mainContainer.removeView(myTargetAdView)
            initAdMobView()
        }

        override fun onShow(p0: MyTargetView) {
            myTargetAdView.visibility = View.VISIBLE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider.AndroidViewModelFactory(activity!!.application).create(MangaAboutViewModel::class.java)
//        viewModel = ViewModelProviders.of(this).get(MangaAboutViewModel::class.java)
        viewModel.currentManga = currentManga
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieInfoBinding.inflate(inflater, container, false)

        showProgressBar()
        if (savedInstanceState != null) {
            val url = savedInstanceState.getString(URL_MANGA)
            viewModel.getMangaFromDatabase(url!!) { movie ->
                startLoading()
            }
        } else {
            if (viewModel.currentManga == null && currentManga != null)
                viewModel.currentManga = currentManga

            startLoading()
        }

        return binding.root
    }

    private fun startLoading() {
        var genresString = StringBuilder()

        for (genre in viewModel.currentManga?.genres ?: ArrayList())
            genresString.append("$genre, ")

        if (genresString.isNotEmpty())
            mangaGenresView.text =
                getString(R.string.genres_description) + " " + genresString.substring(
                    0,
                    genresString.length - 2
                )


        mangaTitleTextView.text = viewModel.currentManga?.title
        mangaProductionCountryView.text =
            getString(R.string.production_country_description) + " " + viewModel.currentManga?.productionCountry
        mangaChaptersNumberView.text = viewModel.currentManga?.chaptersNumber
        mangaDurationView.text = viewModel.currentManga?.duration
        mangaAgeView.text =
            getString(R.string.age_description) + " " + viewModel.currentManga?.productionYear
        mangaDescriptionView.text = viewModel.currentManga?.description


        if (viewModel.currentManga?.mangaImageURL != null && context != null)
            Glide
                .with(context!!)
                .load(viewModel.currentManga?.mangaImageURL!!)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                //.placeholder(R.drawable.loading_spinner)
                .into(mangaImageAbout)

        //initMyTargetAdView()
        initAdMobView()

        dismissProgressBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MANGA, viewModel.currentManga?.url)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshBannerAd()
    }

    private fun showProgressBar() {
        progressCircle.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        progressCircle.visibility = View.GONE
    }

    private fun initMyTargetAdView() {
        //myTargetAdView.adSize = MyTargetView.AdSize.BANNER_300x250
        // myTargetAdView.adSize = AdSize.flexibleSize()
        myTargetAdView.init(Settings.MYTARGET_ID, MyTargetView.AdSize.BANNER_300x250)

        myTargetAdView.listener = myTargetViewListener
        //myTargetAdView.blockId = Settings.BLOCK_ID1
        //myTargetAdView.adEventListener = mBannerAdListener

        //mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        mainContainer.addView(myTargetAdView, layoutParams)
    }

    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.BLOCK_ID1
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        mainContainer.addView(mAdMobView, layoutParams)

        mAdMobView.loadAd(mAdRequest)
    }

    private fun refreshBannerAd() {
        myTargetAdView.visibility = View.INVISIBLE
        mAdMobView.visibility = View.INVISIBLE

        myTargetAdView.load()
        mAdMobView.loadAd(mAdRequest)
    }

    override fun onDestroy() {
        myTargetAdView.destroy()
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


