package com.lucasjwilber.freenote

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.lucasjwilber.freenote.activities.EditListActivity
import com.lucasjwilber.freenote.activities.EditNoteActivity
import com.lucasjwilber.freenote.models.NoteDescriptor

class AllNotesAdapter(private var allNoteDescriptors: List<NoteDescriptor>, private var context: Context) :
    RecyclerView.Adapter<AllNotesAdapter.NoteDescriptorViewHolder>() {

    class NoteDescriptorViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NoteDescriptorViewHolder {
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_descriptor, parent, false) as CardView

        return NoteDescriptorViewHolder(
            cardView
        )
    }

    override fun onBindViewHolder(holder: NoteDescriptorViewHolder, position: Int) {
        val titleTextView: TextView = holder.cardView.findViewById(R.id.noteDescriptorTextView)
        val title = allNoteDescriptors[position].title
        titleTextView.text = title
        holder.cardView.setOnClickListener { goToNoteDetails(holder)}
    }

    private fun goToNoteDetails(holder: NoteDescriptorViewHolder) {
        val id = allNoteDescriptors[holder.adapterPosition].id
        val type = allNoteDescriptors[holder.adapterPosition].type

        val destination =
            if (type == NOTE) EditNoteActivity::class.java
            else EditListActivity::class.java

        val intent = Intent(context, destination)
        intent.putExtra("id", id)
        context.startActivity(intent)
    }

    override fun getItemCount() = allNoteDescriptors.size



}