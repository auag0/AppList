package io.github.auag0.applist.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.appbar.MaterialToolbar
import io.github.auag0.applist.R

class SearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialToolbar(context, attrs, defStyleAttr) {

    private var editText: EditText
    private var searchIcon: ImageView
    private var clearButton: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.search_bar, this)

        editText = findViewById(R.id.editText)
        searchIcon = findViewById(R.id.search_icon)
        clearButton = findViewById(R.id.clear_text)

        searchIcon.setOnClickListener {
            requestFocusIfNeeded()
        }

        clearButton.setOnClickListener {
            editText.text = null
            requestFocusIfNeeded()
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearButton.visibility = when (s.isEmpty()) {
                    true -> View.GONE
                    else -> View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun addTextChangedListener(textWatcher: TextWatcher) {
        editText.addTextChangedListener(textWatcher)
    }

    private fun requestFocusIfNeeded() {
        editText.requestFocus()
        showKeyboard()
    }

    private fun showKeyboard() {
        val inputMethodManager = context.getSystemService(InputMethodManager::class.java)
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
}