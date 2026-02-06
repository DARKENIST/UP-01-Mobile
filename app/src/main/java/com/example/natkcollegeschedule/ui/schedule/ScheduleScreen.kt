package com.example.natkcollegeschedule.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.natkcollegeschedule.data.dto.ScheduleByDateDto
import com.example.natkcollegeschedule.data.repository.ScheduleRepository
import com.example.natkcollegeschedule.data.dto.GroupDto
import com.example.natkcollegeschedule.data.network.RetrofitInstance
import com.example.natkcollegeschedule.utils.getWeekDateRange
import com.example.natkcollegeschedule.data.local.FavoritesDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    favoritesDataStore: FavoritesDataStore,
    initialGroup: String? = "БИ-11",
    modifier: Modifier = Modifier
) {
    var schedule by remember { mutableStateOf<List<ScheduleByDateDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var groups by remember { mutableStateOf<List<GroupDto>>(emptyList()) }
    var selectedGroup by remember { mutableStateOf(initialGroup ?: "") }
    var searchText by remember { mutableStateOf(initialGroup ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Функция для загрузки расписания
    val loadScheduleForGroup = remember {
        { groupName: String ->
            if (groupName.isEmpty()) return@remember

            loading = true
            error = null
            coroutineScope.launch {
                try {
                    val repository = ScheduleRepository(RetrofitInstance.api)
                    val (start, end) = getWeekDateRange()
                    schedule = repository.loadSchedule(groupName, start, end)
                    selectedGroup = groupName
                    // Проверяем статус избранного
                    isFavorite = favoritesDataStore.checkIsFavorite(groupName)
                } catch (e: Exception) {
                    error = "Ошибка загрузки: ${e.message}"
                    schedule = emptyList()
                } finally {
                    loading = false
                }
            }
        }
    }

    // Отслеживаем изменение избранного статуса
    LaunchedEffect(selectedGroup) {
        if (selectedGroup.isNotEmpty()) {
            coroutineScope.launch {
                isFavorite = favoritesDataStore.checkIsFavorite(selectedGroup)
            }
        }
    }

    // Загружаем список групп при первом запуске
    LaunchedEffect(Unit) {
        try {
            val repository = ScheduleRepository(RetrofitInstance.api)
            groups = repository.loadAllGroups()

            // Если есть initialGroup, загружаем её расписание
            if (initialGroup != null && initialGroup.isNotEmpty()) {
                loadScheduleForGroup(initialGroup)
            }
        } catch (e: Exception) {
            error = "Ошибка загрузки групп: ${e.message}"
        }
    }

    // Фильтруем группы по поисковому запросу
    val filteredGroups = if (searchText.isBlank()) {
        groups
    } else {
        groups.filter { group ->
            group.name.contains(searchText, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                        expanded = true
                    },
                    label = { Text("Найдите группу") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Введите название группы") }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (filteredGroups.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (searchText.isNotBlank()) "Совпадений не найдено"
                                    else "Загрузка групп..."
                                )
                            },
                            onClick = { expanded = false }
                        )
                    } else {
                        filteredGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    searchText = group.name
                                    selectedGroup = group.name
                                    expanded = false
                                    loadScheduleForGroup(group.name)
                                }
                            )
                        }
                    }
                }
            }

            if (selectedGroup.isNotEmpty()) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            if (isFavorite) {
                                favoritesDataStore.removeFavoriteGroup(selectedGroup)
                            } else {
                                favoritesDataStore.addFavoriteGroup(selectedGroup)
                            }
                            isFavorite = !isFavorite
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Ошибка", style = MaterialTheme.typography.titleMedium)
                        Text(error!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (selectedGroup.isNotEmpty()) {
                                    loadScheduleForGroup(selectedGroup)
                                }
                            }
                        ) {
                            Text("Повторить")
                        }
                    }
                }
            }
            schedule.isEmpty() && !loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Расписание не найдено", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                ScheduleList(schedule)
            }
        }
    }
}