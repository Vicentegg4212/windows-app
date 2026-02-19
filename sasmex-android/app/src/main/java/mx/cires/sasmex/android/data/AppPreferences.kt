package mx.cires.sasmex.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sasmex_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        private val LAST_NOTIFIED_ALERT_ID = stringPreferencesKey("last_notified_alert_id")
        private val THEME = stringPreferencesKey("theme") // "dark", "light", "system"
    }

    val lastNotifiedAlertId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_NOTIFIED_ALERT_ID] ?: ""
    }

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME] ?: "system"
    }

    suspend fun getLastNotifiedAlertId(): String = lastNotifiedAlertId.first()

    suspend fun getTheme(): String = theme.first()

    suspend fun setLastNotifiedAlertId(id: String) {
        context.dataStore.edit { it[LAST_NOTIFIED_ALERT_ID] = id }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[THEME] = value }
    }
}
