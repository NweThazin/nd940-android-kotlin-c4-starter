package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var dataSource: FakeDataSource

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val reminder1 = ReminderDTO(
        "Reminder1",
        "Description1",
        "Location1",
        1.37,
        103.84
    )
    private val reminder2 = ReminderDTO(
        "Reminder2",
        "Description2",
        "Location2",
        103.84,
        1.37
    )
    private val remindersList = mutableListOf(reminder1, reminder2)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource(remindersList)
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun clearAllValues() {
        viewModel.onClear()
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun saveReminder_thenReturnSuccess() {
        val reminderDataItem = ReminderDataItem(
            "ReminderDataItem",
            "DescriptionDataItem",
            "LocationDataItem",
            1.37,
            103.84
        )
        mainCoroutineRule.pauseDispatcher()

        viewModel.saveReminder(reminderDataItem)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(context.getString(R.string.reminder_saved))
        )
    }

    @Test
    fun titleIsEmpty_assignErrorMessageToSnackBar() {
        val reminderDataItem = ReminderDataItem(
            null,
            "DescriptionDataItem",
            "LocationDataItem",
            1.37,
            103.84
        )

        viewModel.validateEnteredData(reminderDataItem)
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
    }

    @Test
    fun locationIsEmpty_assignErrorMessageToSnackBar() {
        val reminderDataItem = ReminderDataItem(
            "title1",
            "description1",
            null,
            1.37,
            103.84
        )
        viewModel.validateEnteredData(reminderDataItem)
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
    }

    @Test
    fun titleAndEmptyNotEmpty_returnTrue() {
        val reminderDataItem = ReminderDataItem(
            "title1",
            "description1",
            "location1",
            1.37,
            103.84
        )
        val result = viewModel.validateEnteredData(reminderDataItem)
        assertThat(result, `is`(true))
    }

    @Test
    fun validateDataAndSave_returnSuccess() {
        val reminderDataItem = ReminderDataItem(
            "ReminderDataItem",
            "DescriptionDataItem",
            "LocationDataItem",
            1.37,
            103.84
        )
        mainCoroutineRule.pauseDispatcher()

        viewModel.validateAndSaveReminder(reminderDataItem)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            `is`(context.getString(R.string.reminder_saved))
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}