package com.rhetorica.app.widget

import androidx.annotation.StringRes
import com.rhetorica.app.R

enum class WidgetImagePreset(
    val key: String,
    @StringRes val labelRes: Int,
) {
    None("none", R.string.widget_image_none),
    Parchment("parchment", R.string.widget_image_parchment),
    Marble("marble", R.string.widget_image_marble),
    Midnight("midnight", R.string.widget_image_midnight),
    Velvet("velvet", R.string.widget_image_velvet),
    Gallery("gallery", R.string.widget_image_gallery),
    ;

    companion object {
        fun fromKey(key: String?): WidgetImagePreset {
            return entries.firstOrNull { it.key == key } ?: None
        }
    }
}
