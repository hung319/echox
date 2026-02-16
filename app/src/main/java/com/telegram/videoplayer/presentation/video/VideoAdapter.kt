package com.telegram.videoplayer.presentation.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.telegram.videoplayer.R
import com.telegram.videoplayer.databinding.ItemVideoBinding
import com.telegram.videoplayer.domain.model.Video
import com.telegram.videoplayer.presentation.util.formatDuration
import com.telegram.videoplayer.presentation.util.formatFileSize

class VideoAdapter(
    private val onVideoClick: (Video) -> Unit
) : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVideoClick(getItem(position))
                }
            }
        }
        
        fun bind(video: Video) {
            binding.videoTitle.text = video.title
            binding.videoDuration.text = video.duration.toLong().formatDuration()
            binding.videoSize.text = video.fileSize.formatFileSize()
            
            binding.videoThumbnail.load(video.thumbnailUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_video_placeholder)
                error(R.drawable.ic_video_placeholder)
            }
            
            // Progress indicator would be set here if we have progress data
            binding.progressBar.progress = 0
        }
    }
}

class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
        return oldItem == newItem
    }
}
