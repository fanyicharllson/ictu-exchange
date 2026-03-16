package com.fanyiadrien.ictu_ex.data.model

/**
 * Marketplace listing categories.
 * Used in HomeScreen filter chips and PostItemScreen dropdown.
 */
enum class ListingCategory(val displayName: String) {
    ALL("All"),
    TEXTBOOKS("Textbooks"),
    ELECTRONICS("Electronics"),
    HOSTEL_GEAR("Hostel Gear"),
    UNIFORMS("Uniforms")
}