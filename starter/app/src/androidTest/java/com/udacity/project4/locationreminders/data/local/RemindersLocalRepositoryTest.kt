package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getReminders_ReturnSuccess() = runBlocking {
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        repository.saveReminder(reminder)

        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "location",
            1.37,
            103.84
        )
        repository.saveReminder(reminder1)

        val result = repository.getReminders()

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data.size, `is`(2))
    }

    @Test
    fun getReminder_ReturnSuccess() = runBlocking {
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        repository.saveReminder(reminder)

        val result = repository.getReminder(reminder.id)

        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("location"))
        assertThat(result.data.latitude, `is`(1.37))
        assertThat(result.data.longitude, `is`(103.84))
    }

    @Test
    fun getReminder_ReturnError() = runBlocking {
        val result = repository.getReminder("105")

        assertThat(result is Result.Error, `is`(true))
        result as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun emptyReminders_returnsSuccess() = runBlocking {
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        repository.saveReminder(reminder)

        repository.deleteAllReminders()

        val result = repository.getReminders()
        assertThat(result is Result.Success, `is`(true))
        result as Result.Success

        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun emptyReminders_returnsError() = runBlocking {
        repository.deleteAllReminders()

        val result = repository.getReminder("105")
        assertThat(result is Result.Error, `is`(true))

        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }
}