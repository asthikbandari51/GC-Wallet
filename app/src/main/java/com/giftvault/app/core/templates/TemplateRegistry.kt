package com.giftvault.app.core.templates

import com.giftvault.app.domain.model.CardCategory

object TemplateRegistry {

    private val templates: Map<String, TemplateManifest>
    private val aliases: Map<String, String> = mapOf(
        "amazon" to "amazonprime",
        "amazonprime" to "amazonprime",
        "prime" to "amazonprime",
        "primevideo" to "amazonprime",
        "hotstar" to "jiohotstar",
        "jiohotstar" to "jiohotstar",
        "disneyhotstar" to "jiohotstar",
        "bms" to "bookmyshow1",
        "bookmyshow" to "bookmyshow1",
        "bookmyshow1" to "bookmyshow1",
        "googleplay" to "googleplay",
        "playstore" to "googleplay",
        "gplay" to "googleplay",
        "district" to "district",
        "zomatodistrict" to "district",
        "uber" to "uber",
        "uberrides" to "uber",
        "ubereats" to "uber",
        "swiggy" to "swiggy",
        "swiggygift" to "swiggy",
        "swiggyvoucher" to "swiggy",
        "swiggyinstamart" to "swiggy",
        "instamart" to "swiggy",
        "zomato" to "zomato",
        "zomatogift" to "zomato",
        "zomatovoucher" to "zomato",
        "zomatogold" to "zomato",
        "blinkit" to "blinkit",
        "blinkitgift" to "blinkit",
        "grofers" to "blinkit",
        "croma" to "croma",
        "cromagift" to "croma",
        "ajio" to "ajio",
        "ajiogift" to "ajio",
        "trends" to "trends",
        "reliancetrends" to "trends",
        "trendsfashion" to "trends",
        "mmt" to "makemytrip",
        "makemytrip" to "makemytrip",
        "zepto" to "zepto",
        "zeptogift" to "zepto",
        "amazonpay" to "amazonpay",
        "hppay" to "hppay",
        "hpcl" to "hppay",
        "dominos" to "dominos",
        "dominospizza" to "dominos",
        "flipkart" to "flipkart",
        "flipkartgift" to "flipkart",
        "apollo" to "apollopharmacy",
        "apollopharmacy" to "apollopharmacy",
        "apolloclinic" to "apollopharmacy",
        "kalyan" to "kalyanjewellers",
        "kalyanjewellers" to "kalyanjewellers",
        "lakme" to "lakmesalon",
        "lakmesalon" to "lakmesalon",
        "bigbasket" to "bigbasket",
        "bbdaily" to "bigbasket",
        "decathlon" to "decathlon",
        "cult" to "cultfit",
        "cultfit" to "cultfit",
        "curefit" to "cultfit",
        "gaana" to "gaana",
        "myntra" to "myntra",
        "myntrafashion" to "myntra",
        "polarbear" to "polarbear",
        "reliancedigital" to "reliancedigital"
    )

    init {
        val list = listOf(
            TemplateManifest("reliancedigital", "Reliance Digital", "reliancedigital", "logo_reliancedigital", "Electronics", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("apollopharmacy", "Apollo Pharmacy", "apollopharmacy", "logo_apollopharmacy", "Pharmacy", true, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFF111111),
            TemplateManifest("lakmesalon", "Lakme Salon", "lakmesalon", "logo_lakmesalon", "Spa & Salon", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("bigbasket", "BigBasket", "bigbasket", "logo_bigbasket", "Groceries", true, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFF111111),
            TemplateManifest("kalyanjewellers", "Kalyan Jewellers", "kalyanjewellers", "logo_kalyanjewellers", "Jewellery", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("decathlon", "Decathlon", "decathlon", "logo_decathlon", "Sports", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("amazonprime", "Amazon Prime", "amazonprime", "logo_amazonprime", "OTT & Streaming", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("jiohotstar", "JioHotstar", "jiohotstar", "logo_jiohotstar", "OTT & Streaming", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("cultfit", "Cult.fit", "cultfit", "logo_cultfit", "Fitness", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("gaana", "Gaana", "gaana", "logo_gaana", "Music", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("myntra", "Myntra", "myntra", "logo_myntra", "Fashion", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("bookmyshow1", "BookMyShow", "bookmyshow1", "logo_bookmyshow", "Entertainment", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("bookmyshow", "BookMyShow", "bookmyshow1", "logo_bookmyshow", "Entertainment", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("polarbear", "Polar Bear", "polarbear", "logo_polarbear", "Food & Dining", true, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFF111111),
            TemplateManifest("other", "Other", "other", "logo_other", "Others", true, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFF111111),
            TemplateManifest("blinkit", "Blinkit", "blinkit", "logo_blinkit", "Groceries", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("swiggy", "Swiggy", "swiggy", "logo_swiggy", "Food Delivery", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("zomato", "Zomato", "zomato", "logo_zomato", "Food Delivery", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("croma", "Croma", "croma", "logo_croma", "Electronics", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("ajio", "AJIO", "ajio", "logo_ajio", "Fashion", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("trends", "Trends (Reliance)", "trends", "logo_trends", "Fashion", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("makemytrip", "MakeMyTrip", "makemytrip", "logo_makemytrip", "Travel", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("zepto", "Zepto", "zepto", "logo_zepto", "Quick Commerce", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("amazonpay", "Amazon Pay", "amazonpay", "logo_amazonpay", "Shopping", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("flipkart", "Flipkart", "flipkart", "logo_flipkart", "Shopping", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("hppay", "HP Pay", "hppay", "logo_hppay", "Payments", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("dominos", "Domino's", "dominos", "logo_dominos", "Food Delivery", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),

            // ── Newly Added Templates ──
            TemplateManifest("googleplay", "Google Play", "googleplay", "logo_googleplay", "Entertainment", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("district", "District", "district", "logo_district", "Food & Dining", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("uber", "Uber", "uber", "logo_uber", "Travel", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),

            // ── Category Fallback Templates ──
            TemplateManifest("electronics_other", "Electronics (Other)", "electronics_other", "logo_electronics_other", "Electronics", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("shopping_other", "Shopping (Other)", "shopping_other", "logo_shopping_other", "Shopping", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("food_other", "Food & Dining (Other)", "food_other", "logo_food_other", "Food & Dining", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF),
            TemplateManifest("travel_other", "Travel (Other)", "travel_other", "logo_travel_other", "Travel", false, TextZone(10, 10, 100, 20), TextZone(10, 40, 100, 20), TextZone(10, 70, 100, 20), 0xFFFFFFFF)
        )
        templates = list.associateBy { it.templateKey }
    }

    fun getTemplate(key: String): TemplateManifest? = templates[key]

    fun getTemplateForMerchant(merchantName: String): TemplateManifest? {
        val normalized = merchantName.lowercase().replace(Regex("[^a-z0-9]"), "")
        if (normalized.isBlank()) return null
        return templates[normalized] 
            ?: aliases[normalized]?.let { templates[it] }
            ?: templates.values.firstOrNull { 
                val dName = it.displayName.lowercase().replace(Regex("[^a-z0-9]"), "")
                normalized.contains(dName) || dName.contains(normalized)
            }
    }

    fun getCategoryForMerchant(merchantName: String): CardCategory {
        val template = getTemplateForMerchant(merchantName)
        if (template != null) {
            return CardCategory.fromDisplayName(template.category)
        }
        val lower = merchantName.lowercase()
        return when {
            lower.contains("uber") || lower.contains("ola") || lower.contains("rapido") || lower.contains("makemytrip") || lower.contains("flight") || lower.contains("hotel") || lower.contains("travel") || lower.contains("yatra") || lower.contains("cleartrip") -> CardCategory.TRAVEL
            lower.contains("district") -> CardCategory.FOOD_DINING
            lower.contains("google") || lower.contains("play") || lower.contains("playstore") -> CardCategory.ENTERTAINMENT
            lower.contains("myntra") || lower.contains("ajio") || lower.contains("trends") || lower.contains("zara") || lower.contains("h&m") || lower.contains("clothing") -> CardCategory.FASHION
            lower.contains("prime") || lower.contains("hotstar") || lower.contains("netflix") || lower.contains("ott") || lower.contains("sonyliv") || lower.contains("zee5") -> CardCategory.OTT_STREAMING
            lower.contains("apollo") || lower.contains("pharma") || lower.contains("med") || lower.contains("netmeds") || lower.contains("1mg") || lower.contains("thyrocare") -> CardCategory.PHARMACY
            lower.contains("blinkit") || lower.contains("zepto") || lower.contains("instamart") -> CardCategory.QUICK_COMMERCE
            lower.contains("bigbasket") || lower.contains("grocery") || lower.contains("groceries") || lower.contains("supermarket") -> CardCategory.GROCERIES
            lower.contains("swiggy") || lower.contains("zomato") || lower.contains("dominos") || lower.contains("pizza") || lower.contains("burger") || lower.contains("eats") -> CardCategory.FOOD_DELIVERY
            lower.contains("croma") || lower.contains("digital") || lower.contains("electronic") || lower.contains("apple") -> CardCategory.ELECTRONICS
            lower.contains("kalyan") || lower.contains("tanishq") || lower.contains("jewel") || lower.contains("malabar") -> CardCategory.JEWELLERY
            lower.contains("lakme") || lower.contains("salon") || lower.contains("spa") || lower.contains("parlour") -> CardCategory.SPA_SALON
            lower.contains("cult") || lower.contains("gym") || lower.contains("fitness") -> CardCategory.FITNESS
            lower.contains("bookmyshow") || lower.contains("movie") || lower.contains("cinema") || lower.contains("pvr") || lower.contains("inox") -> CardCategory.ENTERTAINMENT
            lower.contains("gaana") || lower.contains("spotify") || lower.contains("music") -> CardCategory.MUSIC
            lower.contains("decathlon") || lower.contains("sports") -> CardCategory.SPORTS
            lower.contains("pay") || lower.contains("fuel") || lower.contains("petrol") || lower.contains("hp") || lower.contains("ioc") -> CardCategory.PAYMENTS
            lower.contains("flipkart") || lower.contains("amazon") || lower.contains("shopping") -> CardCategory.SHOPPING
            else -> CardCategory.OTHERS
        }
    }

    fun getAllTemplates(): List<TemplateManifest> = templates.values.toList()

    fun getTemplatesByCategory(category: String): List<TemplateManifest> {
        return templates.values.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getFallbackTemplate(category: CardCategory? = null): TemplateManifest {
        return when (category) {
            CardCategory.ELECTRONICS -> templates["electronics_other"] ?: templates["other"]!!
            CardCategory.SHOPPING, CardCategory.FASHION -> templates["shopping_other"] ?: templates["other"]!!
            CardCategory.FOOD_DINING, CardCategory.FOOD_DELIVERY -> templates["food_other"] ?: templates["other"]!!
            CardCategory.TRAVEL -> templates["travel_other"] ?: templates["other"]!!
            else -> templates["other"]!!
        }
    }

    private val drawableResCache = java.util.concurrent.ConcurrentHashMap<String, Int>()

    fun getDrawableResId(context: android.content.Context, templateKey: String): Int {
        if (templateKey.isBlank() || templateKey == "other") return 0
        drawableResCache[templateKey]?.let { return it }
        val resId = context.resources.getIdentifier(templateKey, "drawable", context.packageName)
        drawableResCache[templateKey] = resId
        return resId
    }
}
