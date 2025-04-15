package com.example.tales.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.tales.R

/**
 * Classe utilitaire contenant des fonctions d'aide pour l'application
 */
object Utils {
    
    /**
     * Vérifie si l'appareil est connecté à Internet
     * 
     * @param context Le contexte de l'application
     * @return true si l'appareil est connecté à Internet, false sinon
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Charge une image depuis une URL dans une ImageView avec gestion du cache
     * 
     * @param context Le contexte de l'application
     * @param imageUrl L'URL de l'image à charger
     * @param imageView L'ImageView dans laquelle charger l'image
     */
    fun loadImage(context: Context, imageUrl: String, imageView: ImageView) {
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.baby_placeholder)
            .error(R.drawable.baby_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        
        Glide.with(context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(imageView)
    }
    
    /**
     * Précharge une image pour une utilisation ultérieure
     * 
     * @param context Le contexte de l'application
     * @param imageUrl L'URL de l'image à précharger
     */
    fun preloadImage(context: Context, imageUrl: String) {
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .preload()
    }
}
