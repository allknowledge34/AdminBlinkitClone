package com.sachin.adminblinkitclone.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sachin.adminblinkitclone.databinding.ItemViewImageSelectionBinding

class AdapterSelectedImage(val imageUri : ArrayList<Uri>) : RecyclerView.Adapter<AdapterSelectedImage.SelectedImageViewHolder>() {
    class SelectedImageViewHolder (val binding: ItemViewImageSelectionBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdapterSelectedImage.SelectedImageViewHolder {
        return SelectedImageViewHolder(ItemViewImageSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(
        holder: AdapterSelectedImage.SelectedImageViewHolder,
        position: Int
    ) {
        val image = imageUri[position]
        holder.binding.apply {
            ivImage.setImageURI(image)
        }
        holder.binding.closeButton.setOnClickListener {
            if (position< imageUri.size){
                imageUri.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return imageUri.size
    }
}