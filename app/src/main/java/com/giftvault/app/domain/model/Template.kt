package com.giftvault.app.domain.model

data class Template(
    val templateKey: String,
    val displayName: String,
    val category: String,
    val isLightTemplate: Boolean
)
