package com.lucasjwilber.freenote

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.models.Note
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class NoteDaoTests {
    private lateinit var noteDao: NoteDao
    private lateinit var db: NoteDatabase
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun createDb() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(
            context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .build()
        noteDao = db.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_canGetNoteById() = testScope.runBlockingTest {
        val id = 5792489L
        val noteToBeInserted = Note(id, NOTE, "title", "text", 999)
        noteDao.insert(noteToBeInserted)

        // getNoteById() returns LiveData so a utility method is used to get the value
        assertEquals(noteDao.getNoteById(id).getOrAwaitValue(), noteToBeInserted)
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_insertGeneratesAndReturnsAnIDfromNull() = testScope.runBlockingTest {
        val noteToBeInserted = Note(null, NOTE, "title", "text", 999)

        assertNotEquals(noteDao.insert(noteToBeInserted), null)
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_canUpdateExistingNotes() = testScope.runBlockingTest {
        val id = 232314132L
        val note = Note(id, NOTE, "abc", "123", 999)
        noteDao.insert(note)

        // verify note was inserted with title "abc" and segments "123"
        val noteInserted: Note = noteDao.getNoteById(id).getOrAwaitValue()
        assertEquals("abc", noteInserted.title)
        assertEquals("123", noteInserted.segments)

        // alter title and update in db
        note.title = "def"
        note.segments = "456"
        noteDao.update(note)

        // retrieve note and verify updates were saved
        val noteUpdated = noteDao.getNoteById(id).getOrAwaitValue()
        assertEquals(noteUpdated.title, "def")
        assertEquals(noteUpdated.segments, "456")
    }

    @Test
    @Throws(Exception::class)
    fun noteDatabase_canDeleteNotes() = testScope.runBlockingTest {
        val id = 928342L
        val note = Note(id, NOTE, "delete test", "123", 999)
        noteDao.insert(note)

        // verify the note was inserted
        assertEquals("delete test", noteDao.getNoteById(id).getOrAwaitValue().title)

        noteDao.deleteNoteById(note.id!!)

        // verify the note was deleted
        assertNull(noteDao.getNoteById(id).getOrAwaitValue())
    }
}