package com.lucasjwilber.freenote

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.lucasjwilber.freenote.database.NoteDao
import com.lucasjwilber.freenote.database.NoteDatabase
import com.lucasjwilber.freenote.database.NoteRepository
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class NoteRepositoryTests {
    private lateinit var noteDao: NoteDao
    private lateinit var db: NoteDatabase
    private lateinit var repo: NoteRepository
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
        repo = NoteRepository(noteDao, context)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun noteRepository_canInsertAndGetNoteById() = testScope.runBlockingTest {
        val note = Note(NOTE, "title", "text")
        val id = repo.insert(note)

        // getNoteById() returns LiveData so a utility method is used to get the value
        assertEquals(repo.getNoteById(id).getOrAwaitValue(), note)
    }

    @Test
    @Throws(Exception::class)
    fun noteRepository_insertGeneratesAndReturnsAnID() = testScope.runBlockingTest {
        val note = Note(NOTE, "title", "text")

        assertNotEquals(repo.insert(note), null)
    }

    @Test
    @Throws(Exception::class)
    fun noteRepository_canUpdateExistingNotes() = testScope.runBlockingTest {
        var note = Note(NOTE, "abc", "123")
        val id: Long = repo.insert(note)

        // verify note was inserted with title "abc" and segments "123"
        note = repo.getNoteById(id).getOrAwaitValue()
        assertEquals("abc", note.title)
        assertEquals("123", note.segments)

        // alter title and update in db
        note.title = "def"
        note.segments = "456"
        repo.update(note)

        // retrieve note and verify updates were saved
        note = repo.getNoteById(id).getOrAwaitValue()
        assertEquals(note.title, "def")
        assertEquals(note.segments, "456")
    }

//    @Test
//    @Throws(Exception::class)
//    fun noteRepository_updateMethodUpdatesTimestamp() = testScope.runBlockingTest {
//        var note = Note(NOTE, "abc", "123")
//        val id: Long = repo.insert(note)
//        println(Date().time)
//        val firstTimestamp = repo.getNoteById(id).getOrAwaitValue().timestamp
//
//        note.title = "xyz"
//        println(Date().time)
//
//        runBlockingTest {
//            repo.update(note)
//        }
//        println(Date().time)
//        val secondTimestamp = repo.getNoteById(id).getOrAwaitValue().timestamp
//
//        println("first is $firstTimestamp second is $secondTimestamp")
//        println(Date().time)
//        assert(secondTimestamp > firstTimestamp)
//    }

    @Test
    @Throws(Exception::class)
    fun noteRepository_canDeleteNotes() = testScope.runBlockingTest {
        val note = Note(NOTE, "delete test", "123")
        val id: Long = repo.insert(note)

        // verify the note was inserted
        assertEquals("delete test", repo.getNoteById(id).getOrAwaitValue().title)

        repo.deleteNoteById(id)

        // verify the note was deleted
        assertNull(repo.getNoteById(id).getOrAwaitValue())
    }

}