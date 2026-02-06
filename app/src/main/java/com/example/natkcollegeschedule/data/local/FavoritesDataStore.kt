package com.example.natkcollegeschedule.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class FavoritesDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")
        private val FAVORITE_GROUPS = stringSetPreferencesKey("favorite_groups")
    }

    val favoriteGroups: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITE_GROUPS] ?: emptySet()
        }

    suspend fun addFavoriteGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS] ?: emptySet()
            preferences[FAVORITE_GROUPS] = currentFavorites + groupName
        }
    }

    suspend fun removeFavoriteGroup(groupName: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_GROUPS] ?: emptySet()
            preferences[FAVORITE_GROUPS] = currentFavorites - groupName
        }
    }

    suspend fun checkIsFavorite(groupName: String): Boolean {
        val favorites = context.dataStore.data
            .map { preferences ->
                preferences[FAVORITE_GROUPS] ?: emptySet()
            }
            .first()
        return favorites.contains(groupName)
    }
}