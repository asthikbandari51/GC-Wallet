package com.giftvault.app.data.mappers

import com.giftvault.app.core.database.entity.TemplateEntity
import com.giftvault.app.domain.model.Template

fun TemplateEntity.toDomain(): Template {
    return Template(
        templateKey = this.templateKey,
        displayName = this.displayName,
        category = this.category,
        isLightTemplate = this.isLightTemplate
    )
}
