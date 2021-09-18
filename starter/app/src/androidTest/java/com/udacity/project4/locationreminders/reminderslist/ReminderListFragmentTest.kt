package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val dataSource: ReminderDataSource by inject()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    // Test Navigation Fragment
    @Test
    fun clickSaveButton_showSaveReminderScreen() = runBlockingTest {
        // GIVEN
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        dataBindingIdlingResource.monitorFragment(scenario)

        // WHEN
        onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        // Then
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    // Test the displayed data on the UI
    @Test
    fun showLocationReminder_DisplayedInUI() {
        runBlocking {
            dataSource.deleteAllReminders()

            // GIVEN
            val reminder = ReminderDTO(
                "Title",
                "Description",
                "Location",
                1.12,
                1.23
            )
            dataSource.saveReminder(reminder)

            // WHEN
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

            Thread.sleep(5000)

            // THEN
            onView(withText("Title")).check(matches(isDisplayed()))
            onView(withText("Description")).check(matches(isDisplayed()))
            onView(withText("Location")).check(matches(isDisplayed()))
        }
    }

    //  Add testing for the error messages
    @Test
    fun loadReminder_returnEmptyList() {
        runBlocking {
            dataSource.deleteAllReminders()

            // GIVEN
            val result = dataSource.getReminders()

            // WHEN
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)

            onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        }
    }

}