package com.example.data.api

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await

object FirebaseHelper {
    private const val TAG = "FirebaseHelper"
    
    var isInitialized = false
        private set

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            Log.d(TAG, "Initializing Firebase application parameters dynamically...")
            
            // Programmatic Firebase Setup using the coordinates from firebase-applet-config
            val options = FirebaseOptions.Builder()
                .setProjectId("trim-radius-9wh20")
                .setApplicationId("1:1019189914688:android:a3edd32b31790a26fb03a5") // Structured Android Client ID style
                .setApiKey("AIzaSyCzJyaFY0OU_lLsA3RGrkkqu5ZjgPJEs0E")
                .setStorageBucket("trim-radius-9wh20.firebasestorage.app")
                .build()

            FirebaseApp.initializeApp(context.applicationContext, options)
            
            // Setup Firestore settings with offline persistence
            val db = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
                db.firestoreSettings = settings
            } catch (p: Exception) {
                // Settings might already be locked on reincarnation
            }

            isInitialized = true
            Log.d(TAG, "Dynamic Firebase framework initialized!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing programmatic Google services", e)
        }
    }

    val auth: FirebaseAuth?
        get() = if (isInitialized) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isInitialized) FirebaseFirestore.getInstance() else null

    val currentUser: FirebaseUser?
        get() = auth?.currentUser
}
