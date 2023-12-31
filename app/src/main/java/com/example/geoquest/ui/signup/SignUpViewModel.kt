package com.example.geoquest.ui.quest

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log


/**
 * ViewModel
 */
class SignUpViewModel(private val sharedPreferences: SharedPreferences): ViewModel() {
    private val editor = sharedPreferences.edit()

    /**
     * Holds current sign up ui state
     */
    var signUpState by mutableStateOf(
        SignUpState(
            userName = getUserName() ?: "",
            isEntryValid = validateInput(getUserName() ?: "")
        )
    )
        private set

    fun updateSignUpState(userName: String) {
        signUpState =
            SignUpState(userName = userName, isEntryValid = validateInput(userName))
    }

    private fun validateInput(userName: String = signUpState.userName): Boolean {
        return userName.isNotBlank() && isValidText(userName) && userName.length <= 36
    }

    fun saveUserName(userName: String) {
        Log.d("SignUpViewModel", "saveUserName: $userName")
        editor.putString("userName", userName).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("userName", null)
    }

    private fun isValidText(text: String): Boolean {
        return text.matches(Regex("(?=.*[a-zA-Z])[a-zA-Z0-9 ]+"))
    }
}

data class SignUpState(
    val userName: String = "",
    val isEntryValid: Boolean = false
)