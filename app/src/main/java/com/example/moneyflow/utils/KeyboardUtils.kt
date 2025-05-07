package com.example.moneyflow.utils

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.setupBottomViewKeyboardVisibilityListener(bottomView: View) {
    var originalBottomMargin = -1

    ViewCompat.setOnApplyWindowInsetsListener(bottomView) { v, insets ->
        val keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

        val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
        layoutParams?.let {
            if (originalBottomMargin == -1) {
                originalBottomMargin = it.bottomMargin
            }

            if (keyboardVisible) {
                it.bottomMargin = keyboardHeight - navigationBarHeight + originalBottomMargin
            } else {
                it.bottomMargin = originalBottomMargin
            }
            v.layoutParams = it
        }
        insets
    }
    ViewCompat.requestApplyInsets(bottomView)
}