package com.giftvault.app.data.repository

import com.giftvault.app.core.templates.TemplateManifest
import com.giftvault.app.core.templates.TemplateRegistry
import javax.inject.Inject

interface TemplateRepository {
    fun getTemplate(key: String): TemplateManifest?
    fun getTemplateForMerchant(merchantName: String): TemplateManifest?
    fun getAllTemplates(): List<TemplateManifest>
    fun getTemplatesByCategory(category: String): List<TemplateManifest>
}

class TemplateRepositoryImpl @Inject constructor() : TemplateRepository {
    override fun getTemplate(key: String): TemplateManifest? {
        return TemplateRegistry.getTemplate(key)
    }

    override fun getTemplateForMerchant(merchantName: String): TemplateManifest? {
        return TemplateRegistry.getTemplateForMerchant(merchantName)
    }

    override fun getAllTemplates(): List<TemplateManifest> {
        return TemplateRegistry.getAllTemplates()
    }

    override fun getTemplatesByCategory(category: String): List<TemplateManifest> {
        return TemplateRegistry.getTemplatesByCategory(category)
    }
}

