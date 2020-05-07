package com.lucasjwilber.freenote

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lucasjwilber.freenote.databinding.ActivityAllNotesBinding
import kotlinx.coroutines.*
import kotlin.collections.ArrayList
import kotlin.collections.List

class AllNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllNotesBinding
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var allNotes: List<NoteDescriptor> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllNotesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //init RecyclerView with empty list first. the list is populated in onResume
        viewManager = LinearLayoutManager(this)
        viewAdapter = AllNotesAdapter(allNotes, applicationContext)
        binding.allNotesReyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        binding.createNoteButton.setOnClickListener { goToCreateNoteActivity() }
    }

    override fun onResume() {
        super.onResume()
        val notesDao = AppDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO)).noteDao()
        // cache the context so that we can pass it into the RecyclerView adapter
        val context = this

        GlobalScope.launch(Dispatchers.Main) {
            val titles: List<NoteDescriptor> = async(Dispatchers.IO) {
                return@async notesDao.getAllDescriptors()
            }.await()
            allNotes = titles
            Log.i("ljw", "got titles: " + titles.toString())

            viewAdapter = AllNotesAdapter(allNotes, context)
            binding.allNotesReyclerView.apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }



    private fun goToCreateNoteActivity() {
        //make intent, go to CreateNote.kt
        intent = Intent(applicationContext, CreateNoteActivity::class.java)
        startActivity(intent)
    }



//    class AllNotesAdapter(private var myDataset: List<String>, context: Context) :
//        RecyclerView.Adapter<AllNotesAdapter.MyViewHolder>() {
//
//        private var context = context
//
//        class MyViewHolder(val constraintLayout: ConstraintLayout) : RecyclerView.ViewHolder(constraintLayout)
//
//        override fun onCreateViewHolder(parent: ViewGroup,
//                                        viewType: Int): AllNotesAdapter.MyViewHolder {
//            val constraintLayout = LayoutInflater.from(parent.context)
//                .inflate(R.layout.note_title, parent, false) as ConstraintLayout
//
//            return MyViewHolder(constraintLayout)
//        }
//
//        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//            val titleTextView: TextView = holder.constraintLayout.findViewById(R.id.noteTitleTextView)
//            val noteId = myDataset[position]
//            titleTextView.text = noteId.substring(13)
//            holder.constraintLayout.setOnClickListener { goToNoteDetails(noteId)}
//        }
//
//        private fun goToNoteDetails(noteTitle: String) {
//            val intent = Intent(context, ViewNoteActivity::class.java)
//            intent.putExtra("noteTitle", noteTitle)
//            context.startActivity(intent)
//        }
//
//        override fun getItemCount() = myDataset.size
//    }
}
