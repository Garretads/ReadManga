package ru.garretech.readmanga.ui.main


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.navigation.NavigationView
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import ru.garretech.readmanga.BuildConfig
import ru.garretech.readmanga.DisposableManager
import ru.garretech.readmanga.R
import ru.garretech.readmanga.Settings
import ru.garretech.readmanga.ui.about.AboutApplicationActivity
import ru.garretech.readmanga.ui.genres.GenresActivity
import ru.garretech.readmanga.ui.mangaInfo.MangaInfoActivity
import ru.garretech.readmanga.ui.settings.SettingsActivity
import ru.garretech.readmanga.adapters.RecyclerAdapter
import ru.garretech.readmanga.fragments.ConfirmationFragment
import ru.garretech.readmanga.fragments.CustomLoadMoreView
import ru.garretech.readmanga.fragments.DisclaimerFragment
import ru.garretech.readmanga.fragments.SortingFragment
import ru.garretech.readmanga.models.Manga
import ru.garretech.readmanga.tools.SiteWorker
import java.util.*

class MainActivity : AppCompatActivity(), BaseQuickAdapter.OnItemClickListener,
    MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener,
    BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener,
    SortingFragment.OnFragmentInteractionListener {

    private lateinit var searchView: SearchView
    private var mangaAdapter: RecyclerAdapter? = null
    private lateinit var menu: Menu

    private val sortingMenuItem by lazy { menu.findItem(R.id.action_sort) }
    private val clearMenuItem by lazy { menu.findItem(R.id.action_clear) }
    private val searchMenuItem by lazy { menu.findItem(R.id.action_search) }

    private var bag: CompositeDisposable = CompositeDisposable()
    private lateinit var viewModel: MainActivityViewModel

    private var activityState =
        ACTIVITY_STATE.LOST_CONNECTION

    val getMangaListObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {}

            override fun onComplete() {
                DisposableManager.add(
                    viewModel.observable!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateListConsumer)
                )
            }

            override fun onError(e: Throwable) {
                Log.e("List observer", "Failed to get manga list", e)
            }
        }
    }


    private val getOnLoadMoreObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {}

            override fun onComplete() {
                DisposableManager.add(
                    viewModel.observable!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onLoadMoreConsumer)
                )
            }

            override fun onError(e: Throwable) {
                Log.e("ONLOAD MORE OBSERVER", "Failed to perform on load more request", e)
            }
        }
    }


    private val onLoadMoreConsumer by lazy {
        Consumer<List<Manga>> { mangas ->
            mangaAdapter!!.addData(mangas)
            mangaAdapter!!.loadMoreComplete()
        }
    }

    private val updateListConsumer by lazy {
        Consumer<List<Manga>> { mangas ->
            updateDataList(mangas, true)

            if (swipeContainer.isRefreshing) swipeContainer.isRefreshing = false

            dismissProgressBar()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider.AndroidViewModelFactory(application)
            .create(MainActivityViewModel::class.java)

        navigationView.setNavigationItemSelectedListener(this)
        swipeContainer.setOnRefreshListener(this)

//        setSupportActionBar(mainAppbar as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        val metrics = resources.displayMetrics
        val spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        Settings.max_loaded_in_screen = spanCount * 8

        movieListRecyclerView!!.layoutManager = GridLayoutManager(this, spanCount)
        movieListRecyclerView!!.setHasFixedSize(true)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDrawerClosed(drawerView: View) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDrawerOpened(drawerView: View) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mangaAdapter = RecyclerAdapter(R.layout.cardview_manga, ArrayList())
        movieListRecyclerView!!.adapter = mangaAdapter
        mangaAdapter!!.onItemClickListener = this
        mangaAdapter!!.setOnLoadMoreListener(this, movieListRecyclerView)
        mangaAdapter!!.setEnableLoadMore(false)
        mangaAdapter!!.setLoadMoreView(CustomLoadMoreView())

        firstStartDisclaimer()

        //refreshBannerAd()
    }

    private fun showProgressBar() {
        if (!viewModel.progressBottomSheet.isAdded) {
            viewModel.progressBottomSheet.show(supportFragmentManager, "progressBar")
        }
    }

    private fun dismissProgressBar() {
        if (viewModel.progressBottomSheet.isAdded)
            viewModel.progressBottomSheet.dismissAllowingStateLoss()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        this.menu = menu
        val myActionMenuItem = menu.findItem(R.id.action_search)
        searchView = myActionMenuItem.actionView as SearchView

        performInitialRequest()

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String): Boolean {

                if (hasConnection()) {

                    changeState(ACTIVITY_STATE.MOVIE_LIST)
                    showProgressBar()

                    val params = HashMap<String, String>()
                    params["q"] = queryString

                    viewModel.getRequestQueryCompletable(
                        SiteWorker.SEARCH_QUERY,
                        SiteWorker.SEARCH_PREFIX,
                        params
                    )
                        .subscribe(getMangaListObserver)

                    title = getString(R.string.search_hint) + ": $queryString"
                    viewModel.title = title.toString()

                    //if (!searchView.isIconified) {
                    searchView.isIconified = true
                    // }
                } else
                    showConnectionError()

                searchMenuItem.collapseActionView()

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_sort -> {
                if (viewModel.requestQuery!!.requestUri() != null) {

                    showProgressBar()

                    DisposableManager.add(
                        Single.create<JSONArray> { observer ->
                            val jsonArray =
                                SiteWorker.getSortingParams(viewModel.requestQuery?.requestUri()?.build()!!)
                            observer.onSuccess(jsonArray)
                        }.observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ jsonArray ->
                                val sortingFragment = SortingFragment.newInstance(
                                    jsonArray,
                                    viewModel.requestQuery?.requestUri()?.toString()!!
                                )
                                sortingFragment.show(supportFragmentManager, "sortingFragment")

                                dismissProgressBar()

                            }, { Log.d("SORTING FRAGMENT", "CAN'T GET SORTING WINDOW") })
                    )
                }
            }
            R.id.action_clear -> {

                var confirmationDialog: ConfirmationFragment? = null

                when (activityState) {
                    ACTIVITY_STATE.FAVORITES -> {

                        confirmationDialog = ConfirmationFragment.newInstance(
                            "Предупреждение",
                            "Вы действительно хотите удалить содержимое избранного?"
                        )
                        //confirmationDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog)

                        confirmationDialog.setConfirmationListener(object :
                            ConfirmationFragment.OnFragmentInteractionListener {
                            override fun onAcceptPressed() {
                                showProgressBar()

                                DisposableManager.add(viewModel.clearFavorites().subscribe {
                                    viewModel.historyObservable.subscribe(getMangaListObserver)
                                })
                            }

                            override fun onCancelPressed() {}
                        })

                    }
                    ACTIVITY_STATE.HISTORY -> {

                        confirmationDialog = ConfirmationFragment.newInstance(
                            "Предупреждение",
                            "Вы действительно хотите удалить содержимое истории?"
                        )
                        //confirmationDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog)

                        confirmationDialog.setConfirmationListener(object :
                            ConfirmationFragment.OnFragmentInteractionListener {
                            override fun onAcceptPressed() {
                                showProgressBar()

                                DisposableManager.add(viewModel.clearHistory().subscribe {
                                    viewModel.historyObservable.subscribe(getMangaListObserver)
                                })
                            }

                            override fun onCancelPressed() {}
                        })
                    }
                }

                confirmationDialog?.show(supportFragmentManager, "confirmationDialog")
            }
        }
        return true
    }

    override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_editorchoice -> {
                if (hasConnection()) {
                    changeState(ACTIVITY_STATE.EDITOR_CHOICE)

                    showProgressBar()

                    viewModel.getRequestQueryCompletable(SiteWorker.EDITOR_CHOICE_QUERY)
                        .subscribe(getMangaListObserver)

                    title = getString(R.string.editor_choice_title)
                    viewModel.title = title.toString()

                } else showConnectionError()
            }
            R.id.nav_list -> {

                if (hasConnection()) {
                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    showProgressBar()

                    viewModel.getRequestQueryCompletable(
                        SiteWorker.SIMPLE_QUERY,
                        SiteWorker.LIST_PREFIX
                    )
                        .subscribe(getMangaListObserver)

                    title = getString(R.string.list_manga_title)
                    viewModel.title = title.toString()

                } else showConnectionError()
            }

            R.id.nav_random -> {

                if (hasConnection()) {

                    val intent = Intent(this@MainActivity, MangaInfoActivity::class.java)

                    intent.putExtra("is_random", true)

                    startActivity(intent)

                } else showConnectionError()
            }

            R.id.nav_genres -> {

                if (hasConnection()) {

                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    showProgressBar()

                    viewModel.getGenresList().subscribe({
                        val intent = Intent(this@MainActivity, GenresActivity::class.java)
                        intent.putExtra("genres", it.toString())
                        startActivityForResult(intent,
                            GENRES_CODE
                        )
                        dismissProgressBar()
                    }, {
                        Log.e("MainActivity", "Ошибка при получении списка жанров", it)
                        Toast.makeText(
                            this,
                            "Ошибка при получении списка жанров, попробуйте еще раз",
                            Toast.LENGTH_SHORT
                        )

                    })
                } else showConnectionError()
            }
            R.id.nav_favourites -> {
                changeState(ACTIVITY_STATE.FAVORITES)

                viewModel.favoritesObservable.subscribe(getMangaListObserver)

                title = getString(R.string.action_favorite)
            }

            R.id.nav_history -> {
                changeState(ACTIVITY_STATE.HISTORY)

                viewModel.historyObservable.subscribe(getMangaListObserver)

                title = getString(R.string.action_history)
                viewModel.title = title.toString()
            }

            R.id.nav_about -> {
                val intent = Intent(this@MainActivity, AboutApplicationActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GENRES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val resultPrefix = data!!.getStringExtra("link")
                val genreName = data.getStringExtra("name")

                showProgressBar()

                changeState(ACTIVITY_STATE.MOVIE_LIST)

                viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, resultPrefix)
                    .subscribe(getMangaListObserver)

                title = genreName.substring(0, 1).toUpperCase() + genreName.substring(1)
            }
        }
    }


    public override fun onPause() {
        super.onPause()

        dismissProgressBar()
    }

    public override fun onResume() {
        super.onResume()

        dismissProgressBar()

        //myTargetAdView?.resume()
    }


    public override fun onStop() {
        super.onStop()

        dismissProgressBar()
    }

    public override fun onDestroy() {
        super.onDestroy()
        bag.dispose()
    }


    override fun onFragmentInteraction(result: Map<String, Any>) {

        if (hasConnection()) {

            changeState(ACTIVITY_STATE.MOVIE_LIST)

            showProgressBar()

            val params = result.get("params") as HashMap<String, String>

            val completable = if (params.size == 0)
                viewModel.getRequestQueryCompletable(
                    SiteWorker.SIMPLE_QUERY,
                    result["path"] as String
                )
            else
                viewModel.getRequestQueryCompletable(
                    SiteWorker.SIMPLE_QUERY,
                    result["path"] as String,
                    params
                )

            completable.subscribeOn(Schedulers.io())
                .subscribe(getMangaListObserver)

        } else showConnectionError()
    }


    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val selectedManga = mangaAdapter!!.data[position]
        if (hasConnection()) {

            val intent = Intent(this@MainActivity, MangaInfoActivity::class.java)
            intent.putExtra("is_random", false)
            intent.putExtra("manga_url", selectedManga.url)

            startActivity(intent)
        } else showConnectionError()
    }


    override fun onLoadMoreRequested() {
        if (activityState == ACTIVITY_STATE.EDITOR_CHOICE || activityState == ACTIVITY_STATE.MOVIE_LIST) {
            if (viewModel.requestQuery != null) {

                if (viewModel.requestQuery!!.offset() >= viewModel.requestQuery!!.queryAmount()) {
                    mangaAdapter!!.loadMoreComplete()
                    mangaAdapter!!.setEnableLoadMore(false)
                } else {
                    if (hasConnection()) {

                        viewModel.nextQueryObservable.subscribe(getOnLoadMoreObserver)

                    } else {
                        //Get more data failed
                        Toast.makeText(
                            this@MainActivity,
                            R.string.cant_connect_error,
                            Toast.LENGTH_LONG
                        ).show()
                        mangaAdapter!!.loadMoreFail()

                    }
                }
            } else {
                if (mangaAdapter!!.isLoading)
                    mangaAdapter!!.loadMoreComplete()
                mangaAdapter!!.setEnableLoadMore(false)
            }
        } else {
            if (mangaAdapter!!.isLoading)
                mangaAdapter!!.loadMoreComplete()
            mangaAdapter!!.setEnableLoadMore(false)
        }
    }


    override fun onRefresh() {

        when (activityState) {

            ACTIVITY_STATE.EDITOR_CHOICE, ACTIVITY_STATE.MOVIE_LIST, ACTIVITY_STATE.LOST_CONNECTION -> {
                if (hasConnection()) {
                    viewModel.onRefreshObservableNetwork.subscribe(getMangaListObserver)
                } else showConnectionError()
            }
            ACTIVITY_STATE.FAVORITES -> {
                viewModel.favoritesObservable.subscribe(getMangaListObserver)
            }
            ACTIVITY_STATE.HISTORY -> {
                viewModel.historyObservable.subscribe(getMangaListObserver)
            }
        }
    }

    private fun updateDataList(list: List<Manga>, clear: Boolean) {

        if (clear)
            mangaAdapter?.clear()

        mangaAdapter!!.addAll(list)
        movieListRecyclerView!!.recycledViewPool.clear()

        if (mangaAdapter!!.data.size != 0)
            movieListRecyclerView!!.scrollToPosition(0)

        if (activityState == ACTIVITY_STATE.EDITOR_CHOICE || activityState == ACTIVITY_STATE.MOVIE_LIST) {
            if (viewModel.requestQuery != null && viewModel.requestQuery!!.offset() < viewModel.requestQuery!!.queryAmount())
                mangaAdapter!!.setEnableLoadMore(true)
        }
    }

    internal fun showConnectionError() {
        activityState =
            ACTIVITY_STATE.LOST_CONNECTION
        Toast.makeText(applicationContext, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT)
            .show()
    }

    internal fun hasConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    fun performInitialRequest() {
        if (hasConnection()) {
            changeState(ACTIVITY_STATE.EDITOR_CHOICE)

            if (viewModel.requestQuery != null) {
                viewModel.observable?.subscribe(updateListConsumer)
            } else {
                showProgressBar()

                viewModel.getRequestQueryCompletable(SiteWorker.EDITOR_CHOICE_QUERY)
                    .subscribe(getMangaListObserver)
            }
        } else
            showConnectionError()
    }

    private fun changeState(newState: ACTIVITY_STATE) {
        when (newState) {
            ACTIVITY_STATE.EDITOR_CHOICE -> {
                activityState =
                    ACTIVITY_STATE.EDITOR_CHOICE
                sortingMenuItem.isVisible = false
                clearMenuItem.isVisible = false
            }
            ACTIVITY_STATE.MOVIE_LIST -> {
                activityState =
                    ACTIVITY_STATE.MOVIE_LIST
                sortingMenuItem.isVisible = true
                clearMenuItem.isVisible = false

            }
            ACTIVITY_STATE.LOST_CONNECTION -> {
                activityState =
                    ACTIVITY_STATE.LOST_CONNECTION

            }
            ACTIVITY_STATE.HISTORY -> {
                activityState =
                    ACTIVITY_STATE.HISTORY
                sortingMenuItem.isVisible = false
                clearMenuItem.isVisible = true

            }
            ACTIVITY_STATE.FAVORITES -> {
                activityState =
                    ACTIVITY_STATE.FAVORITES
                sortingMenuItem.isVisible = false
                clearMenuItem.isVisible = true

            }
        }
    }

    private fun showSortingMenu() {
        if (!sortingMenuItem.isVisible)
            sortingMenuItem.isVisible = true
    }

    private fun dismissSortingMenu() {
        sortingMenuItem.isVisible = false
    }

    private fun firstStartDisclaimer() {
        val mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE)
        val APP_FIRST_RUN = "first_run_check"
        var isFirstRun = mSettings.getBoolean(APP_FIRST_RUN, true)
        val currentVersion = BuildConfig.VERSION_CODE
        val savedVersion = mSettings.getInt(Settings.VERSION_CODE, 0)

        if (isFirstRun) {
            val editor = mSettings.edit()
            editor.putBoolean(APP_FIRST_RUN, false)
            editor.apply()

            val disclaimerFragment =
                DisclaimerFragment.newInstance(getString(R.string.disclaimer_text))
            disclaimerFragment.show(supportFragmentManager, "disclaimer")
        }

        if (currentVersion != savedVersion) {
            val editor = mSettings.edit()
            editor.putInt(Settings.VERSION_CODE, currentVersion)
            editor.apply()

            val disclaimerFragment = DisclaimerFragment.newInstance(getString(R.string.changelog))
            disclaimerFragment.show(supportFragmentManager, "changelog")
        }


    }

    companion object {

        val GENRES_CODE = 15

        internal enum class ACTIVITY_STATE {
            EDITOR_CHOICE,
            MOVIE_LIST,
            FAVORITES,
            HISTORY,
            LOST_CONNECTION
        }

    }


}
