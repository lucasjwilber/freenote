package com.lucasjwilber.freenote

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.models.Note
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class NoteDatabaseTests {
    private lateinit var noteDao: NoteDao
    private lateinit var db: NoteDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, NoteDatabase::class.java).build()
        noteDao = db.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_canGetNoteById() {
        val noteId: Long = 123
        val noteToBeInserted = Note(noteId, NOTE, "title", "text", 999)

//        noteDao.insert(noteToBeInserted)

        val noteRetrievedById = noteDao.getNoteById(noteId).value
        assertEquals(noteRetrievedById, noteToBeInserted)
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_insertGeneratesAnIDfromNull() {
        val noteToBeInserted = Note(null, NOTE, "title", "text", 999)
        GlobalScope.launch(Dispatchers.IO) {
            val id = noteDao.insert(noteToBeInserted)

            assertEquals(id, null)
        }
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_canUpdateExistingNotes() {
        val note = Note(55, NOTE, "abc", "123", 999)
        GlobalScope.launch(Dispatchers.IO) {
            noteDao.insert(note)
            note.title = "def"
            note.segments = "456"
            noteDao.update(note)
            val retrievedNote= noteDao.getNoteById(55).value!!
            assert(retrievedNote.title == "xyz")
            assert(retrievedNote.segments == "456")
            // timestamp should be updated. this test allows for 5 seconds latency
            assert(abs(Date().time - retrievedNote.timestamp) < 5000)
        }
    }

//    @Test
//    @Throws(Exception::class)
//    fun noteDatabase_canDeleteNotes() {
//        val note = Note(55, NOTE, "abc", "123", 999)
//        GlobalScope.launch(Dispatchers.IO) {
//            noteDao.insert(note)
//            noteDao.deleteNoteById(note.id!!)
//        }
//    }
}