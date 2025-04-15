package com.example.tales.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tales.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var comptineAdapter: ComptineAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.loadComptines()
    }
    
    private fun setupRecyclerView() {
        comptineAdapter = ComptineAdapter { comptine ->
            // Naviguer vers l'écran de détail de la comptine
            ComptineDetailActivity.start(this, comptine.id)
        }
        
        binding.recyclerViewComptines.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = comptineAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.comptines.observe(this) { comptines ->
            comptineAdapter.submitList(comptines)
        }
        
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                // Afficher un message d'erreur
                android.widget.Toast.makeText(this, errorMessage, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
