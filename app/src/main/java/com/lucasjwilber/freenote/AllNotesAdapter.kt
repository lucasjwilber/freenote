package com.lucasjwilber.freenote

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AllNotesAdapter(private var allNoteDescriptors: List<NoteDescriptor>, private var context: Context) :
    RecyclerView.Adapter<AllNotesAdapter.NoteDescriptorViewHolder>() {

    class NoteDescriptorViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NoteDescriptorViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_title, parent, false) as CardView

        return NoteDescriptorViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: NoteDescriptorViewHolder, position: Int) {
        val titleTextView: TextView = holder.cardView.findViewById(R.id.noteTitleTextView)
        val title = allNoteDescriptors[position].title
        titleTextView.text = title
        holder.cardView.setOnClickListener { goToNoteDetails(allNoteDescriptors[position].type, allNoteDescriptors[position].id)}
    }

    private fun goToNoteDetails(type: Int, noteId: Int) {
        val intent = Intent(context, EditNoteActivity::class.java)
        currentNote.id = noteId
        currentNote.type = type
        context.startActivity(intent)
    }

    override fun getItemCount() = allNoteDescriptors.size
}