package com.lucasjwilber.freenote.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.CurrentNote
import com.lucasjwilber.freenote.R
import com.lucasjwilber.freenote.currentNote
import com.lucasjwilber.freenote.views.ViewNoteActivity

class AllNotesAdapter(private var allNoteDescriptors: List<NoteDescriptor>, private var context: Context) :
    RecyclerView.Adapter<AllNotesAdapter.NoteDescriptorViewHolder>() {

    class NoteDescriptorViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NoteDescriptorViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_title, parent, false) as CardView

        return NoteDescriptorViewHolder(
            cardView
        )
    }

    override fun onBindViewHolder(holder: NoteDescriptorViewHolder, position: Int) {
        val titleTextView: TextView = holder.cardView.findViewById(R.id.noteTitleTextView)
        val title = allNoteDescriptors[position].title
        titleTextView.text = title
        holder.cardView.setOnClickListener { goToNoteDetails(holder)}
    }

    private fun goToNoteDetails(holder: NoteDescriptorViewHolder) {
        val intent = Intent(context, ViewNoteActivity::class.java)
        currentNote =
            CurrentNote()
        currentNote.id = allNoteDescriptors[holder.adapterPosition].id
        currentNote.type = allNoteDescriptors[holder.adapterPosition].type
        context.startActivity(intent)
    }

    override fun getItemCount() = allNoteDescriptors.size



}