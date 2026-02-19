package com.transporte.equipajeapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "equipaje_prefs")

@Singleton
class PreferencesManager(
    private val context: Context
) {
    companion object {
        private val USER_ID = intPreferencesKey("user_id")
        private val INTERNO = stringPreferencesKey("interno")
        private val NOMBRE = stringPreferencesKey("nombre")
        private val EMPRESA = stringPreferencesKey("empresa")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        
        // Credenciales para Stored Procedures
        private val SP_USUARIO = stringPreferencesKey("sp_usuario")
        private val SP_PASSWORD = stringPreferencesKey("sp_password")
        private val SP_IDSERVICIO = intPreferencesKey("sp_idservicio")
    }

    val userFlow: Flow<UserPreferences?> = context.dataStore.data.map { prefs ->
        if (prefs[USER_ID] != null) {
            UserPreferences(
                id = prefs[USER_ID] ?: 0,
                interno = prefs[INTERNO] ?: "",
                nombre = prefs[NOMBRE] ?: "",
                empresa = prefs[EMPRESA] ?: ""
            )
        } else {
            null
        }
    }

    suspend fun saveUser(
        id: Int,
        interno: String,
        nombre: String,
        empresa: String,
        token: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[INTERNO] = interno
            prefs[NOMBRE] = nombre
            prefs[EMPRESA] = empresa
            prefs[AUTH_TOKEN] = token
        }
    }
    
    /**
     * Guarda las credenciales necesarias para los Stored Procedures
     */
    suspend fun saveCredentials(
        usuario: String,
        password: String,
        idServicio: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[SP_USUARIO] = usuario
            prefs[SP_PASSWORD] = password
            prefs[SP_IDSERVICIO] = idServicio
        }
    }
    
    /**
     * Obtiene las credenciales para los Stored Procedures
     */
    suspend fun getCredentials(): CredentialsPreferences? {
        val prefs = context.dataStore.data.first()
        return if (prefs[SP_USUARIO] != null && prefs[SP_PASSWORD] != null) {
            CredentialsPreferences(
                usuario = prefs[SP_USUARIO] ?: "",
                password = prefs[SP_PASSWORD] ?: "",
                idServicio = prefs[SP_IDSERVICIO] ?: 0
            )
        } else {
            null
        }
    }
    
    suspend fun getUser(): UsuarioPreferences? {
        val prefs = context.dataStore.data.first()
        return if (prefs[USER_ID] != null) {
            UsuarioPreferences(
                id = prefs[USER_ID] ?: 0,
                interno = prefs[INTERNO] ?: "",
                nombre = prefs[NOMBRE] ?: "",
                empresa = prefs[EMPRESA] ?: ""
            )
        } else {
            null
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.first()[AUTH_TOKEN]
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(USER_ID)
            prefs.remove(INTERNO)
            prefs.remove(NOMBRE)
            prefs.remove(EMPRESA)
            prefs.remove(AUTH_TOKEN)
        }
    }
    
    suspend fun clearCredentials() {
        context.dataStore.edit { prefs ->
            prefs.remove(SP_USUARIO)
            prefs.remove(SP_PASSWORD)
            prefs.remove(SP_IDSERVICIO)
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return context.dataStore.data.first()[USER_ID] != null
    }
}

data class UserPreferences(
    val id: Int,
    val interno: String,
    val nombre: String,
    val empresa: String
)

data class CredentialsPreferences(
    val usuario: String,
    val password: String,
    val idServicio: Int
)

data class UsuarioPreferences(
    val id: Int,
    val interno: String,
    val nombre: String,
    val empresa: String
)
