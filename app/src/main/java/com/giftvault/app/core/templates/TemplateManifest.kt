package com.giftvault.app.core.templates

data class TextZone(
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int
)

data class TemplateManifest(
    val templateKey: String,
    val displayName: String,
    val pngAssetName: String,
    val logoAssetName: String,
    val category: String,
    val isLightTemplate: Boolean,
    val numberZone: TextZone,
    val balanceZone: TextZone,
    val expiryZone: TextZone,
    val defaultTextColor: Long
)
