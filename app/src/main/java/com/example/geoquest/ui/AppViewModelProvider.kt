package com.example.geoquest.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.geoquest.GeoQuestApplication
import com.example.geoquest.ui.home.HomeViewModel
import com.example.geoquest.ui.quest.CreateQuestViewModel
import com.example.geoquest.ui.quest.SettingsViewModel
import com.example.geoquest.ui.quest.SignUpViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(geoQuestApplication().container.questRepository)
        }
        initializer {
            CreateQuestViewModel(geoQuestApplication().container.questRepository)
        }
        initializer {
            SignUpViewModel(geoQuestApplication().container.sharedPreferences)
        }
        initializer {
            SettingsViewModel(geoQuestApplication().container.sharedPreferences)
        }
    }
}

fun CreationExtras.geoQuestApplication(): GeoQuestApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GeoQuestApplication)