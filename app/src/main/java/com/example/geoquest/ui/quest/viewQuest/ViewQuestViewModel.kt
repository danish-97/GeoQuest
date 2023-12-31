package com.example.geoquest.ui.quest.viewQuest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoquest.model.QuestRepository
import com.example.geoquest.ui.quest.createQuest.QuestUiState
import com.example.geoquest.ui.quest.createQuest.toQuestUiState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


/**
 * ViewModel to retrieve and update a task from the [QuestRepository]'s data source
 */
class ViewQuestViewModel(
    savedStateHandle: SavedStateHandle,
    private val questRepository: QuestRepository
): ViewModel() {


    /**     * Holds the current task ui state
     */
    var questUiState by mutableStateOf(QuestUiState())
        private set


    var questId: Int = checkNotNull(savedStateHandle[ViewQuestDestination.questIdArgument])


    init {
        viewModelScope.launch {
            questUiState = questRepository.getQuestStream(questId)
                .filterNotNull()
                .first()
                .toQuestUiState(true)
        }
    }


}