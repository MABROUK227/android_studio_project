package com.example.tales.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tales.databinding.ActivityComptineDetailBinding

class ComptineDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityComptineDetailBinding
    private lateinit var viewModel: ComptineDetailViewModel
    
    companion object {
        private const val EXTRA_COMPTINE_ID = "extra_comptine_id"
        
        fun start(context: Context, comptineId: String) {
            val intent = Intent(context, ComptineDetailActivity::class.java).apply {
                putExtra(EXTRA_COMPTINE_ID, comptineId)
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComptineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val comptineId = intent.getStringExtra(EXTRA_COMPTINE_ID) ?: ""
        if (comptineId.isEmpty()) {
            finish()
            return
        }
        
        setupViewModel(comptineId)
        setupListeners()
        observeViewModel()
    }
    
    private fun setupViewModel(comptineId: String) {
        viewModel = ViewModelProvider(this)[ComptineDetailViewModel::class.java]
        viewModel.loadComptine(comptineId)
    }
    
    private fun setupListeners() {
        binding.buttonListen.setOnClickListener {
            viewModel.currentComptine.value?.let { comptine ->
                // Ici, vous pourriez implémenter la lecture audio de la comptine
                android.widget.Toast.makeText(
                    this,
                    "Lecture de la comptine en cours...",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.currentComptine.observe(this) { comptine ->
            binding.textViewTitle.text = comptine.title
            binding.textViewContent.text = comptine.content
        }
        
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                android.widget.Toast.makeText(this, errorMessage, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
