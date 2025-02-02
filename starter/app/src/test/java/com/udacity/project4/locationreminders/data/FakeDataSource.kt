package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val reminders: MutableList<ReminderDTO>) : ReminderDataSource {

    // Create a fake data source to act as a double to the real data source
    private var shouldReturnError = false

    fun setShouldReturnError(errorReturn: Boolean) {
        shouldReturnError = errorReturn
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("GetReminders Exception")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("GetReminder Exception")
        }
        reminders.find { it.id == id }?.let {
            return Result.Success(it)
        }
        return Result.Error("GetReminder Exception")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}