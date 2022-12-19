package com.example.android.guesstheword.screens.game

import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.format.DateUtils
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN)
}

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 2000)
private val NO_BUZZ_PATTERN = longArrayOf(0)


class GameViewModel(private val vibrator: Vibrator) :
    ViewModel() {   // The current word

    companion object {
        private const val DONE = 0L
        private const val ONE_SECOND = 1000L
        private const val COUNTDOWN_TIME = 10000L
    }

    private var timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
        override fun onFinish() {
            _timerValue.value = DONE
            _eventGameFinish.value = true

            buzz(BuzzType.COUNTDOWN_PANIC)
        }

        override fun onTick(p0: Long) {
            _timerValue.value = p0 / ONE_SECOND
        }
    }


    private val _timerValue = MutableLiveData<Long>()
    val timerValue: LiveData<Long> get() = _timerValue

    val currentTimeString: LiveData<String> = Transformations.map(
        timerValue
    ) { time -> DateUtils.formatElapsedTime(time) }

    // The current word
    private val _word = MutableLiveData<String>()

    // The current score
    private val _score = MutableLiveData<Int>()

    val word: LiveData<String> get() = _word
    val score: LiveData<Int> get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean> get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>


    init {
        Log.i("GameViewModel", "GameViewModel created!")
        _score.value = 0
        _word.value = ""
        _eventGameFinish.value = false
        timer.start()
        resetList()
        nextWord()
    }


    override fun onCleared() {
        super.onCleared()
        timer.cancel()
        Log.i("GameViewModel", "GameViewModel destroyed!")
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            _eventGameFinish.value = true
        } else {
            _word.value = wordList.removeAt(0)
        }
    }

    /** Methods for buttons presses **/

    fun onSkip() {
        _score.value = _score.value?.minus(1)
        nextWord()
    }

    fun onCorrect() {
        buzz(BuzzType.CORRECT)
        _score.value = _score.value?.plus(1)
        nextWord()
    }

    fun onGameFinished() {
        buzz(BuzzType.GAME_OVER)
        _eventGameFinish.value = false
    }

    private fun buzz(pattern: BuzzType) {

        vibrator.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern.pattern,
                        -1
                    )
                )
            } else {
                //deprecated in API 26
                vibrator.vibrate(pattern.pattern, -1)
            }
        }
    }
}