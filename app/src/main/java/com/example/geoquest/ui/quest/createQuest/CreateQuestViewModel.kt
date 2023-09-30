package com.example.geoquest.ui.quest.createQuest

import android.net.Uri
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.geoquest.model.Quest
import com.example.geoquest.model.QuestRepository

/**
 * ViewModel to validate and insert quests in the Room database
 */
class CreateQuestViewModel(private val sharedPreferences: SharedPreferences, private val questRepository: QuestRepository): ViewModel() {
    private val editor = sharedPreferences.edit()

    /**
     * Holds current quest ui state
     */
    var questUiState by mutableStateOf(QuestUiState())
        private set

    private var capturedImageUri by mutableStateOf<Uri?>(null)

    var selectedDifficulty by mutableStateOf(1)
        private set


    fun onDifficultySelected(selectedDifficulty: Int) {
        // Update the selected difficulty level in your ViewModel or wherever you need it
        this.selectedDifficulty = selectedDifficulty
    }

    fun updateUiState(questDetails: QuestDetails) {
        questUiState =
            QuestUiState(questDetails = questDetails, isEntryValid = validateInput(questDetails))
    }

    fun setCapturedUri(uri: Uri) {
        capturedImageUri = uri
    }

    private fun validateInput(uiState: QuestDetails = questUiState.questDetails): Boolean {
        return with(uiState) {
            questTitle.isNotBlank() && isValidText(questTitle) &&
                    (isValidText(questDescription) || questDescription.isBlank()) &&
                    questTitle.length <= 36 && questDescription.length <= 256
        }
    }

    /**
     * Inserts a [Quest] in the Room database
     */
    suspend fun saveQuest() {
        val questDetails = questUiState.questDetails
        if (validateInput(questDetails)) {
            val quest = questDetails.toQuest()
            quest.questImageUri = capturedImageUri.toString()
            quest.questDifficulty = selectedDifficulty
            questRepository.addQuest(quest)
        }
    }

    fun isDeveloperOptionsSet(): Boolean {
        return sharedPreferences.getBoolean("developerOptions", false)
    }

    fun getLocation(): Pair<String, String> {
        return Pair(sharedPreferences.getString("latitude", "0.0") ?: "0.0", sharedPreferences.getString("longitude", "0.0") ?: "0.0")
    }

    private fun isValidText(text: String): Boolean {
        return text.matches(Regex("(?=.*[a-zA-Z])[a-zA-Z0-9 ]+"))
    }
}

data class QuestUiState(
    val questDetails: QuestDetails = QuestDetails(),
    val isEntryValid: Boolean = false
)

data class QuestDetails(
    val questId: Int = 0,
    val questTitle: String = "",
    val questDescription: String = "",
    val questDifficulty: Int = 1,
    val questImageUri: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Extension function to convert [Quest] to [QuestUiState]
 */
fun Quest.toQuestUiState(isEntryValid: Boolean = false): QuestUiState = QuestUiState(
    questDetails = this.toQuestDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [QuestUiState] to [Quest]
 */
fun QuestDetails.toQuest(): Quest = Quest(
    questId = questId,
    questTitle = questTitle,
    questDescription = questDescription,
    questDifficulty = questDifficulty,
    questImageUri = questImageUri,
    latitude = latitude,
    longitude = longitude
)

/**
 * Extension function to convert [Quest] to [QuestDetails]
 */
fun Quest.toQuestDetails(): QuestDetails = QuestDetails(
    questId = questId,
    questTitle = questTitle,
    questDescription = questDescription,
    questDifficulty = questDifficulty,
    questImageUri = questImageUri,
    latitude = latitude,
    longitude = longitude
)