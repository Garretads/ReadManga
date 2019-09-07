package ru.garretech.readmanga.fragments

//import android.arch.persistence.room.PrimaryKey
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions


import org.json.JSONException
import ru.garretech.readmanga.R
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.viewmodels.MangaAboutFragmentViewModel
import java.lang.StringBuilder


class MangaAboutFragment : androidx.fragment.app.Fragment() {


    private var currentManga: Manga? = null
    private lateinit var viewModel : MangaAboutFragmentViewModel
    private lateinit var rootView : View

    private val mangaTitleTextView : TextView by lazy { rootView.findViewById<TextView>(R.id.mangaTitleText) }
    private val mangaAgeView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_age_text) }
    private val mangaGenresView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_genres_text) }
    private val mangaProductionCountryView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_production_country_text) }
    private val mangaChaptersNumberView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_chapters_number_text) }
    private val mangaDurationView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_duration_text) }
    private val imageView : ImageView by lazy { rootView.findViewById<ImageView>(R.id.manga_image_about) }
    private val mangaDescriptionView : TextView by lazy { rootView.findViewById<TextView>(R.id.manga_description_text) }

    private val progressCircle : ProgressBar by lazy { rootView.findViewById<ProgressBar>(R.id.mangaInfoProgressCircle) }

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

        dismissProgressBar()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MANGA,viewModel.currentManga?.url)
    }

    private fun showProgressBar() {
        progressCircle?.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        progressCircle?.visibility = View.GONE
    }


    companion object {

        const val URL_MANGA = "manga_url"

        @Throws(JSONException::class)
        fun newInstance(manga: Manga) = MangaAboutFragment().also {
            it.currentManga = manga
        }
    }
}


