package com.seiko.composeclock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.seconds

@Composable
fun rememberClockState(): ClockState {
    val result = rememberSaveable(saver = ClockState.Saver) {
        val currentDateTime = getCurrentSystemDefaultDateTime()
        ClockState(initialDate = currentDateTime.date, initialTime = currentDateTime.time)
    }
    val lifecycle = LocalLifecycleOwner.current
    LaunchedEffect(result) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            result.collect()
        }
    }
    return result
}

@Stable
class ClockState internal constructor(
    initialDate: LocalDate = LocalDate.ZERO,
    initialTime: LocalTime = LocalTime.ZERO,
) {
    var date by mutableStateOf(initialDate)
    var time by mutableStateOf(initialTime)

    suspend fun collect() {
        while (currentCoroutineContext().isActive) {
            val dateTime = getCurrentSystemDefaultDateTime()
            date = dateTime.date
            time = dateTime.time
            delay(1.seconds)
        }
    }

    companion object {
        val Saver = Saver<ClockState, List<String>>(
            save = {
                listOf(
                    it.date.toString(),
                    it.time.toString(),
                )
            },
            restore = {
                ClockState(
                    initialDate = LocalDate.parse(it[0]),
                    initialTime = LocalTime.parse(it[1]),
                )
            },
        )
    }
}

val LocalDate.Companion.ZERO: LocalDate
    get() = LocalDate(1970, 1, 1)

val LocalTime.Companion.ZERO: LocalTime
    get() = LocalTime(0, 0, 0)

fun getCurrentSystemDefaultDateTime(): LocalDateTime {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
