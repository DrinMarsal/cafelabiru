package com.example.cafelabiru

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    var isCharacterPasswordValid = false
    var isEmailFormatValid = false

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    when (inputType) {
                        InputType.TYPE_TEXT_VARIATION_PASSWORD, 129 -> {
                            isCharacterPasswordValid = s.length > 7
                            error = if (isCharacterPasswordValid) null else "Minimum 8 Character"
                        }

                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, 33 -> {
                            isEmailFormatValid =
                                Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matches(s.toString())
                            error = if (isEmailFormatValid) null else "Email Format Invalid"
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

    }
}