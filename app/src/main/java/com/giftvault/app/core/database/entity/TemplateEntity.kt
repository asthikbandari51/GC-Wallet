package com.giftvault.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey @ColumnInfo(name = "template_key") val templateKey: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "is_light_template") val isLightTemplate: Boolean,
    @ColumnInfo(name = "png_asset_path") val pngAssetPath: String,
    @ColumnInfo(name = "logo_asset_path") val logoAssetPath: String
)
