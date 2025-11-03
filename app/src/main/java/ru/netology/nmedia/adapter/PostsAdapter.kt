package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.scaleNumbers
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enumeration.AttachmentType

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onVideo(post: Post)
    fun onSinglePost(post: Post)
    fun onImageFullscreen(post: Post, url : String)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) :
    PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(view, onInteractionListener)
            }
            R.layout.card_ad -> {
                val view = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(view)
            }
            else -> error("unknown view type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind (ad: Ad) {
        Glide.with(binding.image)
            .load("http://10.0.2.2:9999/media/${ad.image}")
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
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

        menu.isVisible = post.ownedByMe

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

        binding.attachmentImageView.setOnClickListener {
            val imageUrl = "http://10.0.2.2:9999/media/${post.attachment?.url}"
            onInteractionListener.onImageFullscreen(post, imageUrl)
        }

        val avatarUrl = "http://10.0.2.2:9999/avatars/${post.authorAvatar}"
        Glide.with(binding.avatar)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_avatar_placeholder)
            .circleCrop()
            .override(100, 100)
            .timeout(6_000)
            .into(binding.avatar)



        if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
            binding.attachmentImageView.visibility = View.VISIBLE

            val imageUrl = "http://10.0.2.2:9999/media/${post.attachment.url}"
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

object PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem) : Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}

