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
import com.lucasjwilber.freenote.models.NoteDescriptor
import junit.framework.Assert
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

        insertTestData()
    }

    private fun insertTestData() = runBlocking {
        repo.insert(Note(NOTE, "note 1", "blah"))
        repo.insert(Note(LIST, "list 1", "blah"))
        repo.insert(Note(NOTE, "note 2", "blah"))
        repo.insert(Note(NOTE, "note 3", "blah"))
        repo.insert(Note(LIST, "list 2", "blah"))
        repo.insert(Note(LIST, "list 3", "blah"))
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
        var note = Note(NOTE, "first title", "first body")
        val id: Long = repo.insert(note)

        // verify note was inserted with title "first title" and segments "first body"
        note = repo.getNoteById(id).getOrAwaitValue()
        assertEquals("first title", note.title)
        assertEquals("first body", note.segments)

        // alter title and update in db
        note.title = "updated title"
        note.segments = "updated body!"
        repo.update(note)

        // retrieve note and verify updates were saved
        note = repo.getNoteById(id).getOrAwaitValue()
        assertEquals(note.title, "updated title")
        assertEquals(note.segments, "updated body!")
    }

    @Test
    @Throws(Exception::class)
    fun noteRepository_updateMethodUpdatesTimestamp() = testScope.runBlockingTest {
        val note = Note(NOTE, "abc", "123")
        val initialTimestamp = note.timestamp
        val id: Long = repo.insert(note)

        val sameNote = repo.getNoteById(id).getOrAwaitValue()
        sameNote.segments = "lalalalala"
        // assert that timestamp hasn't changed until the repository's updated method is used
        assertEquals(initialTimestamp, sameNote.timestamp)

        repo.update(sameNote)
        val updatedTimestamp = repo.getNoteById(id).getOrAwaitValue().timestamp

        assert(updatedTimestamp > initialTimestamp)
    }

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

    @Test
    @Throws(Exception::class)
    fun sortByNotesFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = repo.getAllDescriptorsNotesFirst().getOrAwaitValue()

        assert(sorted[0].type == NOTE)
        assert(sorted[1].type == NOTE)
        assert(sorted[2].type == NOTE)
        assert(sorted[3].type == LIST)
        assert(sorted[4].type == LIST)
        assert(sorted[5].type == LIST)
    }

    @Test
    @Throws(Exception::class)
    fun sortByListsFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = repo.getAllDescriptorsListsFirst().getOrAwaitValue()

        assert(sorted[0].type == LIST)
        assert(sorted[1].type == LIST)
        assert(sorted[2].type == LIST)
        assert(sorted[3].type == NOTE)
        assert(sorted[4].type == NOTE)
        assert(sorted[5].type == NOTE)
    }

    @Test
    @Throws(Exception::class)
    fun sortByOldestFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = repo.getAllDescriptorsOldestFirst().getOrAwaitValue()

        // this method actually sorts by id, which is a generated sequential value
        for (i in 0 until sorted.size - 1) {
            assert(sorted[i].id < sorted[i+1].id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun sortByNewestFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = repo.getAllDescriptorsNewestFirst().getOrAwaitValue()

        // this method actually sorts by id, which is a generated sequential value
        for (i in 0 until sorted.size - 1) {
            assert(sorted[i].id > sorted[i+1].id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun sortByLastUpdatedFirst() = testScope.runBlockingTest {
        repo.insert(Note(NOTE, "note 4", "blah"))
        repo.insert(Note(LIST, "list 4", "blah"))
        repo.insert(Note(NOTE, "note 5", "blah"))

        // this sorting method sorts by timestamp, which is set first in the Note constructor and then
        // in the repository's update method
        val sorted: List<NoteDescriptor> = repo.getAllDescriptorsLastUpdatedFirst().getOrAwaitValue()

        // first verify that the last created/inserted notes & lists appear first.
        Assert.assertEquals(sorted[0].title, "note 5")
        Assert.assertEquals(sorted[1].title, "list 4")
        Assert.assertEquals(sorted[2].title, "note 4")

        // verify notes are sorted by timestamp, descending
        for (i in 0 until sorted.size - 1) {
            val first = repo.getNoteById(sorted[i].id).getOrAwaitValue()
            val second = repo.getNoteById(sorted[i + 1].id).getOrAwaitValue()

            assert(first.timestamp > second.timestamp)
        }
    }

}