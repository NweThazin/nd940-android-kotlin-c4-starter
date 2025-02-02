package com.udacity.project4

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val locationViewModel: SaveReminderViewModel by inject()

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)
    private lateinit var decorView: View

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @Test
    fun addingReminder_showAddedReminder() {
        runBlocking {
            val poi = PointOfInterest(
                LatLng(1.3663111405929946, 103.85272309184074),
                "ChIJAAAAAAAAAAARUqtA0s38gk0",
                "Deyi Secondary School"
            )
            locationViewModel.pointOfInterest = poi
            locationViewModel.latitude.postValue(poi.latLng.latitude)
            locationViewModel.longitude.postValue(poi.latLng.longitude)
            locationViewModel.reminderSelectedLocationStr.postValue(poi.name)
            locationViewModel.selectedPOI.postValue(poi)

            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            onView(withId(R.id.addReminderFAB)).perform(click())
            onView(withId(R.id.reminderTitle)).perform(
                typeText("Title1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.reminderDescription)).perform(
                typeText("Description1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.selectLocation)).perform(click())

            onView(withId(R.id.btn_save)).perform(click())

            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText("Title1")).check(matches(isDisplayed()))
            onView(withText("Description1")).check(matches(isDisplayed()))
            onView(withText("Deyi Secondary School")).check(matches(isDisplayed()))

            activityScenario.close()
        }
    }

    @Test
    fun addReminder_showEmptyTitleErrorMessage() {
        runBlocking {
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            onView(withId(R.id.addReminderFAB)).perform(click())
            onView(withId(R.id.reminderDescription)).perform(
                typeText("Description1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText(R.string.err_enter_title))
                .inRoot(
                    withDecorView(not(decorView))
                )
                .check(matches(isDisplayed()))

            activityScenario.close()
        }
    }

    @Test
    fun addReminder_showEmptyLocationErrorMessage() {
        runBlocking {
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            onView(withId(R.id.addReminderFAB)).perform(click())
            onView(withId(R.id.reminderTitle)).perform(
                typeText("Title1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.reminderDescription)).perform(
                typeText("Description1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.saveReminder)).perform(click())

            onView(withText(R.string.err_select_location))
                .inRoot(
                    withDecorView(not(decorView))
                )
                .check(matches(isDisplayed()))

            activityScenario.close()
        }
    }

    @Test
    fun addReminder_showSelectPOIErrorMessage() {
        runBlocking {
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            onView(withId(R.id.addReminderFAB)).perform(click())
            onView(withId(R.id.reminderTitle)).perform(
                typeText("Title1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.reminderDescription)).perform(
                typeText("Description1"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.selectLocation)).perform(click())
            onView(withId(R.id.btn_save)).perform(click())
            onView(withText(R.string.select_poi))
                .inRoot(
                    withDecorView(not(decorView))
                )
                .check(matches(isDisplayed()))

            activityScenario.close()
        }
    }
}
