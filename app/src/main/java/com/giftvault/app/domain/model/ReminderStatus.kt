package com.giftvault.app.domain.model

enum class ReminderStatus(val value: String) {
    PENDING("pending"), 
    SENT("sent"), 
    SNOOZED("snoozed"), 
    DISMISSED("dismissed");

    companion object {
        fun fromValue(value: String): ReminderStatus = entries.find { it.value == value } ?: PENDING
    }
}
