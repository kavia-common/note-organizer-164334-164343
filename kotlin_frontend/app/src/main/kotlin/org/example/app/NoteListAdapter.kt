package org.example.app

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * PUBLIC_INTERFACE
 * NoteListAdapter renders Note items in the ListView for MainActivity.
 */
class NoteListAdapter(context: Context, private val data: MutableList<Note>) :
    ArrayAdapter<Note>(context, 0, data) {

    fun setData(newData: List<Note>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Note? = data[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_note, parent, false)
        val note = data[position]

        val tvTitle: TextView = v.findViewById(R.id.tvTitle)
        val tvPreview: TextView = v.findViewById(R.id.tvPreview)
        val tvCategory: TextView = v.findViewById(R.id.tvCategory)

        tvTitle.text = if (note.title.isBlank()) context.getString(R.string.empty_note_title) else note.title
        tvCategory.text = note.category ?: ""
        tvPreview.text = if (!TextUtils.isEmpty(note.content)) {
            note.content.trim().replace("\\s+".toRegex(), " ")
        } else {
            ""
        }
        return v
    }
}
