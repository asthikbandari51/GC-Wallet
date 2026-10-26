package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.ReminderEntity
import com.giftvault.app.domain.model.Reminder
import com.giftvault.app.domain.model.ReminderStatus
import java.time.LocalDate

fun ReminderEntity.toDomain(): Reminder {
    return Reminder(
        id = this.id,
        cardId = this.cardId,
        triggerDate = LocalDate.ofEpochDay(this.triggerDate),
        windowDays = this.windowDays,
        status = ReminderStatus.valueOf(this.status.uppercase())
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = this.id,
        cardId = this.cardId,
        triggerDate = this.triggerDate.toEpochDay(),
        windowDays = this.windowDays,
        status = this.status.name.lowercase()
    )
}
