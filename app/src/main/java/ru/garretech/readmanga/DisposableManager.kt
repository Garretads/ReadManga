package ru.garretech.readmanga

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable



class DisposableManager {

    companion object {

    private var INSTANCE: CompositeDisposable? = null

    fun add(disposable: Disposable) {
        getInstance().add(disposable)
    }

    fun dispose() {
        getInstance().dispose()
    }

    private fun getInstance(): CompositeDisposable {
        if (INSTANCE == null || INSTANCE!!.isDisposed) {
            synchronized(DisposableManager::class.java) {
                INSTANCE = CompositeDisposable()
            }
        }
        return INSTANCE!!
    }
}

}