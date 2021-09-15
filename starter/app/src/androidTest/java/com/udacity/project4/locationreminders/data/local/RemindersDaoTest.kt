package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders() = runBlockingTest {
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        database.reminderDao().saveReminder(reminder)
        val reminder1 = ReminderDTO(
            "title1",
            "description1",
            "location1",
            1.37,
            103.84
        )
        database.reminderDao().saveReminder(reminder1)
        val reminder2 = ReminderDTO(
            "title2",
            "description2",
            "location2",
            1.37,
            103.84
        )
        database.reminderDao().saveReminder(reminder2)

        val allReminders = database.reminderDao().getReminders()

        assertThat(3, `is`(allReminders.size))
    }

    @Test
    fun saveReminderTask() = runBlockingTest {
        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminderId = reminder.id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, Matchers.notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {

        val reminder = ReminderDTO(
            "title",
            "description",
            "location",
            1.37,
            103.84
        )
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminderId = reminder.id)

        if (loaded != null) {
            database.reminderDao().deleteAllReminders()
        }

        assertThat(database.reminderDao().getReminders(), `is`(emptyList()))
    }

}