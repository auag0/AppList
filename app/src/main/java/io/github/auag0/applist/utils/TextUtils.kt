package io.github.auag0.applist.utils

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

object TextUtils {
    fun colorizeText(fullText: String, targetText: String, color: Int): Spannable {
        val spannable = SpannableStringBuilder(fullText)
        val startIndex = fullText.indexOf(targetText)
        if (startIndex != -1) {
            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex,
                startIndex + targetText.length,
                0
            )
        }
        return spannable
    }
}