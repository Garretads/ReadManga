package ru.garretech.readmanganew.tools

import android.os.AsyncTask

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.IOException

class PageDownloader : AsyncTask<String, Void, Document?>() {

    override fun doInBackground(vararg strings: String): Document? {
        var result: Document? = null
        try {
            result = Jsoup.connect(strings[0]).get()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }
}

