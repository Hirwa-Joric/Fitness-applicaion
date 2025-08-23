
package com.modarb.android.wear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkoutViewModel : ViewModel() {
    private val _exercise = MutableLiveData<String>()
    val exercise: LiveData<String> = _exercise

    private val _timer = MutableLiveData<String>()
    val timer: LiveData<String> = _timer

    private val _heartRate = MutableLiveData<String>()
    val heartRate: LiveData<String> = _heartRate

    private val _calories = MutableLiveData<String>()
    val calories: LiveData<String> = _calories

    fun updateExercise(exercise: String) {
        _exercise.postValue(exercise)
    }

    fun updateTimer(timer: String) {
        _timer.postValue(timer)
    }

    fun updateHeartRate(heartRate: String) {
        _heartRate.postValue(heartRate)
    }

    fun updateCalories(calories: String) {
        _calories.postValue(calories)
    }
}
