package org.example.app

import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * PUBLIC_INTERFACE
 * ManageCategoriesActivity allows adding and removing categories.
 */
class ManageCategoriesActivity : AppCompatActivity() {

    private val repository: NoteRepository by lazy { NoteRepository.getInstance(this) }

    private lateinit var inputNewCategory: EditText
    private lateinit var btnAdd: MaterialButton
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NoteOrganizer)
        setContentView(R.layout.activity_manage_categories)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        inputNewCategory = findViewById(R.id.input_new_category)
        btnAdd = findViewById(R.id.btn_add_category)
        listView = findViewById(R.id.list_categories)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, repository.getAllCategories().sorted().toMutableList())
        listView.adapter = adapter

        btnAdd.setOnClickListener {
            val text = inputNewCategory.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                repository.addCategory(text)
                inputNewCategory.setText("")
                refresh()
            }
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val item = adapter.getItem(position) ?: return@OnItemLongClickListener true
            repository.removeCategory(item)
            refresh()
            true
        }
    }

    private fun refresh() {
        adapter.clear()
        adapter.addAll(repository.getAllCategories().sorted())
        adapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
