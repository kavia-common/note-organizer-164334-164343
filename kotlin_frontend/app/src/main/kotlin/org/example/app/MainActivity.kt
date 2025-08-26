package org.example.app

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * PUBLIC_INTERFACE
 * MainActivity is the entry point of the Notes app.
 * It displays a list of notes, supports searching and filtering by category,
 * and provides navigation to create/edit/view note screens.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var searchInput: EditText
    private lateinit var categoryFilter: Spinner
    private lateinit var adapter: NoteListAdapter

    private val repository: NoteRepository by lazy { NoteRepository.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Use AppCompatActivity to support Material toolbar and themes
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NoteOrganizer) // ensure our light theme is applied
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        listView = findViewById(R.id.notes_list)
        fabAdd = findViewById(R.id.fab_add)
        searchInput = findViewById(R.id.search_input)
        categoryFilter = findViewById(R.id.category_filter)

        adapter = NoteListAdapter(this, mutableListOf())
        listView.adapter = adapter

        // Populate category spinner with "All" + categories from repo
        setupCategoryFilter()

        // List item click -> open details
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val note = adapter.getItem(position)!!
            val intent = Intent(this, NoteDetailActivity::class.java)
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.id)
            startActivity(intent)
        }

        // Add new note
        fabAdd.setOnClickListener {
            val intent = Intent(this, EditNoteActivity::class.java)
            startActivity(intent)
        }

        // Search as-you-type
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterAndShow() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        categoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) { filterAndShow() }
            override fun onNothingSelected(parent: AdapterView<*>?) { filterAndShow() }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload and display notes when returning to the list
        setupCategoryFilter() // categories may have changed
        filterAndShow()
    }

    private fun setupCategoryFilter() {
        val categories = mutableListOf(getString(R.string.category_all))
        categories.addAll(repository.getAllCategories().sorted())
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryFilter.adapter = spinnerAdapter
    }

    private fun filterAndShow() {
        val query = searchInput.text?.toString().orEmpty()
        val selectedCategory = categoryFilter.selectedItem?.toString() ?: getString(R.string.category_all)
        val data = repository.searchNotes(query, if (selectedCategory == getString(R.string.category_all)) null else selectedCategory)
        adapter.setData(data)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        // Hook into SearchManager if needed for future expansion
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        // Using our own EditText for live search, so no binding here
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_manage_categories -> {
                val intent = Intent(this, ManageCategoriesActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
