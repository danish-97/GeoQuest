package com.example.geoquest.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.geoquest.GeoQuestTopBar
import com.example.geoquest.R
import com.example.geoquest.model.Quest
import com.example.geoquest.ui.AppViewModelProvider
import com.example.geoquest.ui.navigation.NavigationDestination
import com.example.geoquest.ui.quest.createQuest.CreateQuestViewModel
import com.example.geoquest.ui.quest.findQuest.BackPressHandler
import com.example.geoquest.ui.quest.viewQuest.DifficultyStars
import com.example.geoquest.ui.theme.GeoQuestTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

object HomeDestination: NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navigateToCreateQuest: () -> Unit,
    navigateToViewQuest: (Int) -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    createViewModel: CreateQuestViewModel
    ) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val selectedQuestId = remember { mutableIntStateOf(-1) }

    createViewModel.questUiState.questDetails.image = null

    BackPressHandler(onBackPressed = {})

    PermissionRequired(
        permissionState = permissionState,
        permissionNotGrantedContent = {
            // Content to show when permission is not granted
            PermissionsDenied(modifier = modifier, scrollBehavior = scrollBehavior, permissionState = permissionState)
        },
        permissionNotAvailableContent = {
            // Content to show when permission is not available (e.g., policy restrictions)
            PermissionsDenied(modifier = modifier, scrollBehavior = scrollBehavior, permissionState = permissionState)
        }
    ) {
        // Content to show when permission is granted
        Scaffold(
            modifier = modifier,
            topBar = {
                GeoQuestTopBar(
                    title = stringResource(id = HomeDestination.titleRes),
                    canNavigateBack = false,
                    onSettingsClick = navigateToSettings,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = navigateToCreateQuest,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "")
                }
            },
        ) {contentPadding ->
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.4f)
                        .padding(contentPadding)// Takes half of the screen height
                ) {
                    MapTarget(homeUiState.questList, viewModel, selectedQuestId)
                }
                HomeBody(
                    questList = homeUiState.questList,
                    navigateToViewQuest,
                    selectedQuestId,
                    modifier = modifier
                        .fillMaxSize()
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionsDenied(modifier: Modifier, scrollBehavior: TopAppBarScrollBehavior, permissionState: PermissionState) {
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = HomeDestination.titleRes)) },
                modifier = modifier,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = modifier
                .padding(contentPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Location permissions are required to use this app.")
            Button(onClick = { permissionState.launchPermissionRequest() }) {
                Text("Request Permission")
            }
        }
    }
}

@Composable
fun HomeBody(
    questList: List<Quest>,
    navigateToViewQuest: (Int) -> Unit,
    selectedQuestId: MutableIntState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (questList.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_quest),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        } else {
            QuestList(
                questList = questList,
                navigateToViewQuest,
                selectedQuestId
            )
        }
    }
}

@Composable
fun QuestList(
    questList: List<Quest>,
    navigateToViewQuest: (Int) -> Unit,
    selectedQuestId: MutableIntState,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedQuestId.intValue) {
        if (selectedQuestId.intValue != -1) {
            val index = questList.indexOfFirst { quest -> quest.questId == selectedQuestId.intValue }
            listState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(items = questList, key = { it.questId }) { quest ->
            QuestCard(
                quest = quest,
                navigateToViewQuest,
                isSelected = quest.questId == selectedQuestId.intValue,
                selectedQuestId
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestCard(
    quest: Quest,
    navigateToViewQuest: (Int) -> Unit,
    isSelected: Boolean = false,
    selectedQuestId: MutableIntState
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
        onClick = {
            selectedQuestId.intValue = quest.questId
        },
        modifier = Modifier
            .background(
                if (isSelected) if (isSystemInDarkTheme()) Color.Black else Color.LightGray else Color.Transparent
            )
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .shadow(8.dp, shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val painter: Painter = if (quest.questImageUri != null) {
                    rememberAsyncImagePainter(model = quest.questImageUri)
                } else {
                    painterResource(id = R.drawable.default_image)
                }

                Box(modifier = Modifier
                    .fillMaxHeight(0.7F)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = stringResource(id = R.string.default_image),
                        modifier = Modifier.size(130.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = quest.questTitle,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "By: " + quest.author,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    DifficultyStars(quest.questDifficulty)
                    Row {

                        Button(
                            onClick = { navigateToViewQuest(quest.questId) },
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(text = stringResource(id = R.string.view_button))
                        }
                        Button(
                            onClick = { shareQuest(quest, context) },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(Color.Transparent),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Quest"
                            )
                        }
                    }

                }

            }
        }
    }
}


@Composable
fun MapTarget(questList: List<Quest>, viewModel: HomeViewModel, selectedQuestId: MutableIntState){
    // Extract the position from the state
    val positions = questList.map { LatLng(it.latitude, it.longitude) }
    val averageLat = positions.map { it.latitude }.average()
    val averageLng = positions.map { it.longitude }.average()

    val cameraPositionState: CameraPositionState
    if (positions.isEmpty()) {
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(viewModel.getLocation(), 4.5f)
        }
    } else if (selectedQuestId.intValue != -1) {
        val quest = questList.find { quest -> quest.questId == selectedQuestId.intValue }

        val position = if (quest !== null) {
            CameraPosition.fromLatLngZoom(LatLng(quest.latitude, quest.longitude), 4.5f)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(averageLat, averageLng), 4.5f)
        }

        cameraPositionState = CameraPositionState(position = position)
    } else {
        cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(averageLat, averageLng), 4.5f)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true)
    ) {
        for (quest in questList) {
            Marker(
                state = rememberMarkerState(position = LatLng(quest.latitude, quest.longitude)),
                title = quest.questTitle,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                onClick = {
                    selectedQuestId.intValue = quest.questId
                    true
                }
            )

        }
    }
}

fun shareQuest(quest: Quest, context: Context) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(
        Intent.EXTRA_TEXT,
        "Quest Title: ${quest.questTitle}\nQuest Description: ${quest.questDescription}" +
                "\nQuest Difficulty: ${quest.questDifficulty}/5"
    )
    sendIntent.type = "text/plain"

    val chooserIntent = Intent.createChooser(sendIntent, "Share Quest")
    context.startActivity(chooserIntent)
}


//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    GeoQuestTheme {
//        HomeScreen(
//            navigateToCreateQuest = {},
//            navigateToViewQuest = {},
//            navigateToSettings = {},
//
//        )
//    }
//}