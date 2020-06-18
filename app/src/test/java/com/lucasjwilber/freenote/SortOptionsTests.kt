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
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SortOptionsTests {
    private lateinit var noteDao: NoteDao
    private lateinit var db: NoteDatabase
    private lateinit var repo: NoteRepository
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setUp() {
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
        noteDao.insert(Note(NOTE, "note 1", "blah"))
        noteDao.insert(Note(LIST, "list 1", "blah"))
        noteDao.insert(Note(NOTE, "note 2", "blah"))
        noteDao.insert(Note(NOTE, "note 3", "blah"))
        noteDao.insert(Note(LIST, "list 2", "blah"))
        noteDao.insert(Note(LIST, "list 3", "blah"))
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun sortByNotesFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = noteDao.getAllDescriptorsNotesFirst().getOrAwaitValue()

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
        val sorted: List<NoteDescriptor> = noteDao.getAllDescriptorsListsFirst().getOrAwaitValue()

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
        val sorted: List<NoteDescriptor> = noteDao.getAllDescriptorsOldestFirst().getOrAwaitValue()

        // this method actually sorts by id, which is a generated sequential value
        for (i in 0 until sorted.size - 1) {
            assert(sorted[i].id < sorted[i+1].id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun sortByNewestFirst() = testScope.runBlockingTest {
        val sorted: List<NoteDescriptor> = noteDao.getAllDescriptorsNewestFirst().getOrAwaitValue()

        // this method actually sorts by id, which is a generated sequential value
        for (i in 0 until sorted.size - 1) {
            assert(sorted[i].id > sorted[i+1].id)
        }
    }

    @Test
    @Throws(Exception::class)
    fun sortByLastUpdatedFirst() = testScope.runBlockingTest {
//        var note4 = Note(NOTE, "note 4", "blah")
//        var note5 = Note(NOTE, "note 4", "blah")
//        var list4 = Note(LIST, "list 4", "blah")
//        val note5Id: Long = noteDao.insert(note5)
//        val note4Id: Long = noteDao.insert(note4)
//        val list4Id: Long = noteDao.insert(list4)

        // first verify that the last inserted notes/lists appear first
        val sorted: List<NoteDescriptor> = noteDao.getAllDescriptorsLastUpdatedFirst().getOrAwaitValue()

        // this sorting method sorts by timestamp, which is updated in the note constructor and the
        // repository's update method
        for (i in 0 until sorted.size - 1) {
            val note1 = noteDao.getNoteById(sorted[i].id).getOrAwaitValue()
            val note2 = noteDao.getNoteById(sorted[i + 1].id).getOrAwaitValue()
            assert(note1.timestamp > note2.timestamp)
        }

    }

}