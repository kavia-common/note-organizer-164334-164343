package org.example.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * PUBLIC_INTERFACE
 * NoteDetailActivity shows the full note content and allows navigation to edit.
 */
class NoteDetailActivity : AppCompatActivity() {

    private val repository: NoteRepository by lazy { NoteRepository.getInstance(this) }
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NoteOrganizer)
        setContentView(R.layout.activity_note_detail)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra(EXTRA_NOTE_ID)
        note = id?.let { repository.get(it) }

        render()
    }

    private fun render() {
        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvCategory: TextView = findViewById(R.id.tvCategory)
        val tvContent: TextView = findViewById(R.id.tvContent)

        val n = note
        if (n != null) {
            tvTitle.text = if (n.title.isBlank()) getString(R.string.empty_note_title) else n.title
            tvCategory.text = n.category ?: ""
            tvContent.text = n.content
            supportActionBar?.title = getString(R.string.title_note_detail)
        } else {
            tvTitle.text = getString(R.string.empty_note_title)
            tvContent.text = ""
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.menu_edit -> {
                val n = note ?: return true
                val intent = Intent(this, EditNoteActivity::class.java)
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, n.id)
                startActivity(intent)
                true
            }
            R.id.menu_delete -> {
                note?.let { repository.delete(it.id) }
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
