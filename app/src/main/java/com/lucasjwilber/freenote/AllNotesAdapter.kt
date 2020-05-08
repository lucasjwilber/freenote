package com.lucasjwilber.freenote

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class AllNotesAdapter(private var myDataset: List<NoteDescriptor>, context: Context) :
    RecyclerView.Adapter<AllNotesAdapter.MyViewHolder>() {

    private var context = context

    class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AllNotesAdapter.MyViewHolder {
        val constraintLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_title, parent, false) as ConstraintLayout

        return MyViewHolder(constraintLayout)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val titleTextView: TextView = holder.constraintLayout.findViewById(R.id.noteTitleTextView)
        val title = myDataset[position].title
        titleTextView.text = title
        holder.constraintLayout.setOnClickListener { goToNoteDetails(myDataset[position].id)}
    }

    private fun goToNoteDetails(noteId: Int) {
        val intent = Intent(context, CreateNoteActivity::class.java)
        intent.putExtra("noteId", noteId)
        context.startActivity(intent)
    }

    override fun getItemCount() = myDataset.size
}