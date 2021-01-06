package ru.garretech.readmanganew.ui.genres

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_genres.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.garretech.readmanganew.R
import java.util.*

class GenresActivity : AppCompatActivity() {
    private var genresArray: JSONArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genres)

        title = getString(R.string.genres_title)

        if (toolbarActionBar != null) {
            setSupportActionBar(toolbarActionBar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val intent = intent
        val genresNameList = ArrayList<String>()
        try {
            genresArray = JSONArray(intent.getStringExtra("genres"))

            for (i in 0 until genresArray!!.length()) {
                val name = (genresArray!!.get(i) as JSONObject).getString("name")
                genresNameList.add(name.substring(0, 1).toUpperCase() + name.substring(1))
            }

            val arrayAdapter = object : ArrayAdapter<String>(
                applicationContext,
                android.R.layout.simple_list_item_1,
                genresNameList
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val text: TextView = view.findViewById(android.R.id.text1)
                    text.setTextColor(resources.getColor(android.R.color.white))
                    return view
                }
            }

            genresListView.adapter = arrayAdapter
            genresListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
                try {
                    val data = Intent()
                    data.putExtra("link", (genresArray!!.get(i) as JSONObject).getString("link"))
                    data.putExtra("name", (genresArray!!.get(i) as JSONObject).getString("name"))
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_genres, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
