package ru.garretech.readmanga.ui.mangaInfo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_manga_info.*
import ru.garretech.readmanga.R
import ru.garretech.readmanga.ui.settings.SettingsActivity
import ru.garretech.readmanga.adapters.MovieAboutPagerAdapter
import ru.garretech.readmanga.database.AppDataSource
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.tools.SiteWorker

class MangaInfoActivity : AppCompatActivity() {

/*  * Переходим по ссылке http://readmanga.me/tower_of_god/vol3/6
    * Считываем количество страниц из элемента с классом pages-count
    * Берем первое фото из div с id=fotocontext. Содержимое аттрибута src из тега img
    * http://e5.mangas.rocks/auto/30/35/40/TowerOfGod_s3_ch06_p01_SIU_Gemini.jpg_res.jpg?t=1556875730&u=0&h=i1nwNAGZO2AF_mAe3BzlHQ
    * Подставляем вместо p01 номера с 1 по количество страниц. Полученный массив ссылок и будет текущий эпизоп манги
    *
    * */

    internal lateinit var mFragmentAdapter: MovieAboutPagerAdapter
    lateinit var viewModel: MangaInfoActivityViewModel
    var isRandom: Boolean = true

    internal lateinit var currentManga: Manga
    internal lateinit var dataSource: AppDataSource
    internal var observable: Subject<Boolean> = PublishSubject.create()
    internal var disposable: Disposable? = null
    internal lateinit var optionsMenu: Menu


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manga_info)
        viewModel = ViewModelProviders.of(this).get(MangaInfoActivityViewModel::class.java)
        showProgressCircle()

        isRandom = intent.getBooleanExtra("is_random", true)

        var url: String = if (isRandom) {
            SiteWorker.RANDOM_MOVIE_PREFIX
        } else {
            intent.getStringExtra("manga_url")!!
        }

        viewModel.getMangaInfo(url) { prepareManga(it) }
    }


    private fun setupViewPager(viewPager: androidx.viewpager.widget.ViewPager) {
        mFragmentAdapter = MovieAboutPagerAdapter(supportFragmentManager)

        mFragmentAdapter.addFragment(
            MangaAboutFragment.newInstance(viewModel.currentManga!!),
            "О манге"
        )
        mFragmentAdapter.addFragment(
            MangaEpisodesFragment.newInstance(viewModel.currentManga!!),
            "Эпизоды"
        )
        viewPager.adapter = mFragmentAdapter
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = ""
    }

    private fun flagFavorite(flag: Boolean) {
        val item = optionsMenu.getItem(0)
        if (flag)
            item.setIcon(R.drawable.ic_favorite_white_24dp)
        else
            item.setIcon(R.drawable.ic_favorite_border_white_24dp)

    }

    private fun showProgressCircle() {
        infoProgressCircle.visibility = View.VISIBLE
    }

    private fun dismissProgressCircle() {
        infoProgressCircle.visibility = View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun prepareManga(manga: Manga) {
        viewModel.addManga(manga) {
            title = ""

            setupViewPager(viewPager)
            tabLayout.setupWithViewPager(viewPager)

            viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
            dismissProgressCircle()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_manga_about, menu)
        optionsMenu = menu


        disposable = observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t -> flagFavorite(t) }

        viewModel.isInFavorite.subscribe(
            { isInFavorite ->
                emmitFavorite(isInFavorite)
            }, {
                Log.e("MangaInfoActivity", "Ошибка проверки манги в избранном", it)
            })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_favorite -> {
                if (viewModel.isFavorite) {
                    viewModel.deleteFavorites
                        .subscribe({
                            emmitFavorite(false)
                            Log.d("MangaInfoActivity", "Удаление из избранного успешно")
                        }, {
                            Log.e(
                                "MangaInfoActivity",
                                "Удаление из избранного завершилось с ошибкой",
                                it
                            )
                        })
                } else {
                    viewModel.addFavorites
                        .subscribe({
                            emmitFavorite(true)
                            Log.d("MangaInfoActivity", "Добавление в избранное успешно")
                        }, {
                            Log.e(
                                "MangaInfoActivity",
                                "Добавление в избранное завершилось ошибкой",
                                it
                            )
                        })
                }
            }
            R.id.action_settings -> {
                val intent = Intent(this@MangaInfoActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    internal fun emmitFavorite(value: Boolean) {
        viewModel.isFavorite = value
        observable.onNext(value)
    }

}

