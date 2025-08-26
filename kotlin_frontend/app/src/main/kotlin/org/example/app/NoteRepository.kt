package org.example.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * PUBLIC_INTERFACE
 * NoteRepository provides CRUD operations on notes and category set,
 * persisted locally in SharedPreferences as JSON.
 */
class NoteRepository private constructor(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAll(): List<Note> {
        val json = prefs.getString(KEY_NOTES_JSON, "[]") ?: "[]"
        val arr = JSONArray(json)
        val result = mutableListOf<Note>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(jsonToNote(o))
        }
        return result.sortedByDescending { it.updatedAt }
    }

    fun get(id: String): Note? = getAll().firstOrNull { it.id == id }

    fun save(note: Note) {
        val current = getAll().toMutableList()
        val idx = current.indexOfFirst { it.id == note.id }
        note.updatedAt = System.currentTimeMillis()
        if (idx >= 0) current[idx] = note else current.add(note)
        persist(current)
        // also ensure category stored
        note.category?.let { addCategory(it) }
    }

    fun delete(id: String) {
        val current = getAll().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) {
            current.removeAt(idx)
            persist(current)
        }
    }

    fun searchNotes(query: String, category: String? = null): List<Note> {
        val q = query.trim().lowercase()
        return getAll().filter { n ->
            val matchesQuery = if (q.isBlank()) true else
                n.title.lowercase().contains(q) || n.content.lowercase().contains(q)
            val matchesCategory = category?.let { n.category == it } ?: true
            matchesQuery && matchesCategory
        }
    }

    fun getAllCategories(): Set<String> {
        val raw = prefs.getStringSet(KEY_CATEGORIES, emptySet()) ?: emptySet()
        return raw
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val set = getAllCategories().toMutableSet()
        set.add(trimmed)
        prefs.edit().putStringSet(KEY_CATEGORIES, set).apply()
    }

    fun removeCategory(name: String) {
        val set = getAllCategories().toMutableSet()
        set.remove(name)
        prefs.edit().putStringSet(KEY_CATEGORIES, set).apply()
        // Notes keep the category string if still used; not purging references.
    }

    private fun persist(list: List<Note>) {
        val arr = JSONArray()
        list.forEach { arr.put(noteToJson(it)) }
        prefs.edit().putString(KEY_NOTES_JSON, arr.toString()).apply()
    }

    private fun jsonToNote(o: JSONObject): Note {
        return Note(
            id = o.optString("id"),
            title = o.optString("title"),
            content = o.optString("content"),
            category = if (o.has("category") && !o.isNull("category")) o.getString("category") else null,
            createdAt = o.optLong("createdAt"),
            updatedAt = o.optLong("updatedAt")
        )
    }

    private fun noteToJson(n: Note): JSONObject {
        val o = JSONObject()
        o.put("id", n.id)
        o.put("title", n.title)
        o.put("content", n.content)
        o.put("category", n.category)
        o.put("createdAt", n.createdAt)
        o.put("updatedAt", n.updatedAt)
        return o
    }

    companion object {
        private const val PREFS_NAME = "notes_prefs"
        private const val KEY_NOTES_JSON = "notes_json"
        private const val KEY_CATEGORIES = "categories"

        private var INSTANCE: NoteRepository? = null

        // PUBLIC_INTERFACE
        /**
         * Returns a singleton instance of NoteRepository for the given context.
         */
        fun getInstance(context: Context): NoteRepository {
            if (INSTANCE == null) {
                INSTANCE = NoteRepository(context.applicationContext)
            }
            return INSTANCE!!
        }
    }
}
