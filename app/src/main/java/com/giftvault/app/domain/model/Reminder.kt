package com.giftvault.app.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class Reminder(
    val id: String,
    val cardId: String,
    val triggerDate: LocalDate,
    val windowDays: Int,
    val status: ReminderStatus
)
