package com.example.moneyflow.data

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.getDrawableResId(resName: String): Int {
    return resources.getIdentifier(resName, "drawable", packageName)
}

fun Context.getDrawableByName(resName: String): Drawable? {
    val resId = getDrawableResId(resName)
    return if (resId != 0) ContextCompat.getDrawable(this, resId) else null
}