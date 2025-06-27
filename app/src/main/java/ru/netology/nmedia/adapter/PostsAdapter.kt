package ru.netology.nmedia.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.activity.scaleNumbers
import androidx.core.net.toUri
import com.bumptech.glide.Glide

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideo(post: Post)
    fun onSinglePost(post: Post)
    fun onError(exception: Exception)
}

class PostsAdapter(private val onInteractionListener: OnInteractionListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(view, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) = with(binding) {
        author.text = post.author
        published.text = post.published
        content.text = post.content
        likes.apply {
            text = scaleNumbers(post.likes.toString())
            isChecked = post.likedByMe
        }
        shares.text = scaleNumbers(post.shares.toString())
        views.text = scaleNumbers(post.views.toString())

        likes.setOnClickListener {
            onInteractionListener.onLike(post)
        }

        shares.setOnClickListener {
            onInteractionListener.onShare(post)
        }

        if (post.video?.isNotEmpty() == true) {
            video.visibility = Group.VISIBLE
        }

        videoImage.setOnClickListener {
            onInteractionListener.onVideo(post)
        }

        content.setOnClickListener {
            onInteractionListener.onSinglePost(post)
        }

        menu.setOnClickListener {
            PopupMenu(it.context, it).apply {
                inflate(R.menu.post_actions)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.remove -> {
                            onInteractionListener.onRemove(post)
                            true
                        }

                        R.id.edit -> {
                            onInteractionListener.onEdit(post)
                            true
                        }

                        else -> false
                    }
                }
            }.show()
        }

        val avatarUrl = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
        Glide.with(binding.avatar)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_avatar_placeholder)
            .circleCrop()
            .override(100, 100)
            .timeout(5_000)
            .into(binding.avatar)


        if (post.attachment != null && post.attachment.type == "IMAGE") {
            binding.attachmentImageView.visibility = View.VISIBLE

            val imageUrl = "http://10.0.2.2:9999/images/${post.attachment.url}"
            Glide.with(binding.attachmentImageView)
                .load(imageUrl)
                .placeholder(R.drawable.gray_background)
                .error(R.drawable.ic_broken_image)
                .timeout(6_000)
                .into(binding.attachmentImageView)

        } else {
            binding.attachmentImageView.visibility = View.GONE
        }

    }
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}

