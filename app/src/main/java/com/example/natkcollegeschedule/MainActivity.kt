package com.example.natkcollegeschedule
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.natkcollegeschedule.data.api.ScheduleApi
import com.example.natkcollegeschedule.data.local.FavoritesDataStore
import com.example.natkcollegeschedule.data.network.RetrofitInstance
import com.example.natkcollegeschedule.data.repository.ScheduleRepository
import com.example.natkcollegeschedule.ui.favorites.FavoritesScreen
import com.example.natkcollegeschedule.ui.schedule.ScheduleScreen
import com.example.natkcollegeschedule.ui.theme.NATKCollegeScheduleTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NATKCollegeScheduleTheme {
                CollegeScheduleApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun CollegeScheduleApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    var selectedGroupFromFavorites by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val favoritesDataStore = remember { FavoritesDataStore(context) }
    val repository = remember { ScheduleRepository(RetrofitInstance.api) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> ScheduleScreen(
                    favoritesDataStore = favoritesDataStore,
                    initialGroup = selectedGroupFromFavorites, // Передаем группу (может быть null)
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.FAVORITES -> FavoritesScreen(
                    favoritesDataStore = favoritesDataStore,
                    onGroupSelected = { groupName ->
                        selectedGroupFromFavorites = groupName
                        currentDestination = AppDestinations.HOME
                    },
                    modifier = Modifier.padding(innerPadding)
                )

            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Главная", Icons.Default.Home),
    FAVORITES("Любимое", Icons.Default.Favorite)
}