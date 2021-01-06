package ru.garretech.readmanganew.fragments

import com.chad.library.adapter.base.loadmore.LoadMoreView

import ru.garretech.readmanganew.R

class CustomLoadMoreView : LoadMoreView() {
    override fun getLayoutId(): Int {
        return R.layout.fragment_load_more
    }

    override fun isLoadEndGone(): Boolean {
        return true
    }

    override fun getLoadingViewId(): Int {
        return R.id.load_more_loading_view
    }

    override fun getLoadFailViewId(): Int {
        return R.id.load_more_load_fail_view
    }

    /**
     * IsLoadEndGone () for true, you can return 0
     * IsLoadEndGone () for false, can not return 0
     */
    override fun getLoadEndViewId(): Int {
        return 0
    }
}
