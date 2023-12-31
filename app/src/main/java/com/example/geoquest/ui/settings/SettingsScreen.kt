package com.example.geoquest.ui.settings

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geoquest.GeoQuestTopBar
import com.example.geoquest.R
import com.example.geoquest.model.getCurrentLocation
import com.example.geoquest.model.sendNotification
import com.example.geoquest.ui.AppViewModelProvider
import com.example.geoquest.ui.home.HomeDestination
import com.example.geoquest.ui.navigation.NavigationDestination
import com.example.geoquest.ui.theme.GeoQuestTheme

object SettingsDestination: NavigationDestination {
    override val route = "settings"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateUp: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToHomeScreen: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            GeoQuestTopBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateUp

            )
        }
    ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .verticalScroll(scrollState, enabled = true)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.change_username),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_size).value.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                        .widthIn(max = 280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedTextField(
                        value = viewModel.settingsState.userName,
                        onValueChange = {
                            viewModel.updateSettingsState(userName = it)
                        },
                        label = { Text(stringResource(id = R.string.player_name)) },
                        singleLine = true,
                        isError = !isValidText(viewModel.settingsState.userName) || viewModel.settingsState.userName.length > 24,
                        supportingText = {
                            if (viewModel.settingsState.userName.length > 24) {
                                Text(
                                    text = stringResource(
                                        id = R.string.character_limit,
                                        viewModel.settingsState.userName.length,
                                        24
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (!isValidText(viewModel.settingsState.userName)) {
                                Text(
                                    text = stringResource(id = R.string.validation_message),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (viewModel.settingsState.userName.length > 24 || !isValidText(
                                    viewModel.settingsState.userName
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = stringResource(id = R.string.validation_error),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                    )
                }

                DeveloperOptionsToggle(
                    isChecked = viewModel.settingsState.developerOptions,
                    onCheckedChange = { viewModel.updateSettingsState(developerOptions = it) }
                )

                if (viewModel.settingsState.developerOptions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                viewModel.insertTestData(context)
                                Toast.makeText(context, context.resources.getString(R.string.test_data_success), Toast.LENGTH_SHORT).show()
                                navigateToHomeScreen()
                                      },
                            colors = ButtonDefaults.buttonColors(if (isSystemInDarkTheme()) Color.White else Color.DarkGray)
                        ) {
                            Text(
                                text = stringResource(id = R.string.populate)
                            )
                        }
                        Button(onClick = {
                            viewModel.clearData()
                            Toast.makeText(context, context.resources.getString(R.string.clear_data_success), Toast.LENGTH_SHORT).show()
                            navigateToHomeScreen()
                                         },
                            colors = ButtonDefaults.buttonColors(if (isSystemInDarkTheme()) Color.White else Color.DarkGray)
                        ) {
                            Text(
                                text = stringResource(id = R.string.clear)
                            )
                        }
                    }

                    LatLongInput(viewModel.settingsState.latitude, { viewModel.updateSettingsState(
                        latitude = it,
                    ) }, viewModel.settingsState.longitude, {
                        viewModel.updateSettingsState(
                            longitude = it
                        )
                    })
                    Button(onClick = {
                        getCurrentLocation(context, {
                            viewModel.updateSettingsState(
                                latitude = it.latitude.toString(),
                                longitude = it.longitude.toString()
                            )
                        }, {
                            Toast.makeText(context, "Error getting location", Toast.LENGTH_SHORT).show()
                        })
                    }) {
                        Text(
                            text = stringResource(id = R.string.set_current_loc)
                        )
                    }
                    Button(onClick = { sendNotification(context, "TEST", "TESTING") }) {
                        Text(
                            text = "Send notification"
                        )
                    }
                }

                Button(onClick = {
                    viewModel.saveSettings()
                    Toast.makeText(context, context.resources.getString(R.string.user_name_changed), Toast.LENGTH_SHORT).show()
                    navigateToHomeScreen()
                }) {
                    Text(
                        text = stringResource(id = R.string.save)
                    )
                }
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                    )
                }
            }
    }
}

@Composable
fun isValidText(text: String): Boolean {
    return text.isEmpty() || text.matches(Regex("(?=.*[a-zA-Z])[a-zA-Z0-9 ]+"))
}

@Composable
fun DeveloperOptionsToggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(id = R.string.developer_options))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun LatLongInput(
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = latitude,
            onValueChange = onLatitudeChange,
            label = { Text(text = stringResource(id = R.string.latitude)) },
            modifier = Modifier.weight(1f),  // This will make the text fields share the available space equally
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { /* Handle next action */ })
        )
        OutlinedTextField(
            value = longitude,
            onValueChange = onLongitudeChange,
            label = { Text(text = stringResource(id = R.string.longitude)) },
            modifier = Modifier.weight(1f),  // This will make the text fields share the available space equally
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { /* Handle done action */ })
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    GeoQuestTheme {
        SettingsScreen(
            navigateUp = {},
            navigateToHomeScreen = {}
        )
    }
}