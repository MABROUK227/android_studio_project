package com.example.tales

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.example.tales.service.FirebaseService

class TalesApplication : Application(), LifecycleObserver {
    
    companion object {
        private const val TAG = "TalesApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialiser Firebase
        FirebaseService.initialize(this)
        
        // Ajouter l'observateur du cycle de vie
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        Log.d(TAG, "Application initialisée")
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Log.d(TAG, "Application en arrière-plan")
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d(TAG, "Application au premier plan")
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        // Libérer le cache de Glide en cas de mémoire faible
        Glide.get(this).clearMemory()
        Log.d(TAG, "Mémoire faible - Cache Glide nettoyé")
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Réduire l'utilisation de la mémoire en fonction du niveau
        Glide.get(this).trimMemory(level)
        Log.d(TAG, "Trim mémoire niveau $level")
    }
}
