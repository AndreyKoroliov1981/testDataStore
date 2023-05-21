package com.example.testdatastore

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.testdatastore.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//sharedPref
private const val APP_PREFERENCES = "mysettings"
private const val APP_PREFERENCES_NAME = "name"

//proto datastore
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //data store
    private val USER_FIRST_NAME = stringPreferencesKey("user_name")

    private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user"
    )

    //proto data store
    private val Context.userPreferencesStore: DataStore<UserProto> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer
    )

    //EncryptedSharedPreferences
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mySharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)

        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "EncryptedSharedPreferencesName",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        binding.btnSharedPref.setOnClickListener {
            val strNickName = binding.etText.text.toString()
            val editor: SharedPreferences.Editor = mySharedPreferences.edit();
            editor.putString(APP_PREFERENCES_NAME, strNickName)
            editor.apply()
        }


        binding.btnDataStore.setOnClickListener {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        userPreferencesDataStore.edit { preferences ->
                            preferences[USER_FIRST_NAME] = binding.etText.text.toString()
                        }
                    }
                }
            }
        }

        binding.btnProtoDataStore.setOnClickListener {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        userPreferencesStore.updateData  { preferences ->
                            preferences
                                .toBuilder()
                                .setName(binding.etText.text.toString()).build()
                        }
                    }
                }
            }
        }

        binding.btnEncryptedSharedPreferences.setOnClickListener {
            Log.d("my_tag", "btnEncryptedSharedPreferences press")
            val strNickName = binding.etText.text.toString()
            encryptedSharedPreferences.edit()
                .putString("DATA", strNickName)
                .apply()
        }
    }
}