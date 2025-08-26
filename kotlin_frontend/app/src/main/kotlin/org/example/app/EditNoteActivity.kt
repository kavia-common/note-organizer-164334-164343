package org.example.app

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * PUBLIC_INTERFACE
 * EditNoteActivity handles creating a new note or editing an existing one.
 */
class EditNoteActivity : AppCompatActivity() {

    private val repository: NoteRepository by lazy { NoteRepository.getInstance(this) }

    private var note: Note? = null

    private lateinit var inputTitle: EditText
    private lateinit var inputContent: EditText
    private lateinit var spinnerCategory: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NoteOrganizer)
        setContentView(R.layout.activity_edit_note)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        inputTitle = findViewById(R.id.input_title)
        inputContent = findViewById(R.id.input_content)
        spinnerCategory = findViewById(R.id.spinner_category)

        val id = intent.getStringExtra(EXTRA_NOTE_ID)
        note = id?.let { repository.get(it) }

        setupCategories()

        if (note != null) {
            supportActionBar?.title = getString(R.string.title_edit_note)
            inputTitle.setText(note!!.title)
            inputContent.setText(note!!.content)
            selectCategory(note!!.category)
        } else {
            supportActionBar?.title = getString(R.string.title_new_note)
        }

        val btnSave: MaterialButton = findViewById(R.id.btn_save)
        val btnDelete: MaterialButton = findViewById(R.id.btn_delete)

        btnSave.setOnClickListener {
            val n = note ?: Note()
            n.title = inputTitle.text?.toString().orEmpty()
            n.content = inputContent.text?.toString().orEmpty()
            n.category = spinnerCategory.selectedItem?.toString()
            repository.save(n)
            finish()
        }

        btnDelete.setOnClickListener {
            note?.let { repository.delete(it.id) }
            finish()
        }
        // If creating a new note, hide delete
        if (note == null) btnDelete.isEnabled = false
    }

    private fun setupCategories() {
        val categories = repository.getAllCategories().toMutableList()
        if (categories.isEmpty()) {
            // Seed with a default category
            repository.addCategory("Personal")
        }
        val refreshed = repository.getAllCategories().toList()
        val a = ArrayAdapter(this, android.R.layout.simple_spinner_item, refreshed)
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = a
    }

    private fun selectCategory(category: String?) {
        if (category == null) return
        val count = spinnerCategory.adapter?.count ?: return
        for (i in 0 until count) {
            if (spinnerCategory.adapter.getItem(i)?.toString() == category) {
                spinnerCategory.setSelection(i)
                return
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
