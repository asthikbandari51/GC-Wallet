package com.giftvault.app.domain.model

enum class TransactionType(val value: String) {
    REDEEM("redeem"), 
    ADD("add"), 
    ADJUSTMENT("adjustment");

    companion object {
        fun fromValue(value: String): TransactionType = entries.find { it.value == value } ?: ADJUSTMENT
    }
}
