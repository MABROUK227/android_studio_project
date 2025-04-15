package com.example.tales.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tales.databinding.ItemComptineBinding
import com.example.tales.models.Comptine

class ComptineAdapter(private val onItemClick: (Comptine) -> Unit) : 
    ListAdapter<Comptine, ComptineAdapter.ComptineViewHolder>(ComptineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComptineViewHolder {
        val binding = ItemComptineBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComptineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComptineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ComptineViewHolder(private val binding: ItemComptineBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(comptine: Comptine) {
            binding.textViewTitle.text = comptine.title
            binding.textViewDescription.text = comptine.description
        }
    }

    class ComptineDiffCallback : DiffUtil.ItemCallback<Comptine>() {
        override fun areItemsTheSame(oldItem: Comptine, newItem: Comptine): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comptine, newItem: Comptine): Boolean {
            return oldItem == newItem
        }
    }
}
