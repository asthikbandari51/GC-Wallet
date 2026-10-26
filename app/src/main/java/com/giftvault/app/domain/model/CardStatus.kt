package com.giftvault.app.domain.model

enum class CardStatus(val value: String) {
    ACTIVE("active"), 
    USED("used"), 
    EXPIRED("expired"), 
    ARCHIVED("archived");

    companion object {
        fun fromValue(value: String): CardStatus = entries.find { it.value == value } ?: ACTIVE
    }
}
