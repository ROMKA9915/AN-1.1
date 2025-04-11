package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostViewModel
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                if (isImeVisible) imeInsets.bottom else systemBars.bottom
            )
            insets
        }

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            val editPostLauncher = registerForActivityResult(NewPostResultContract) { result ->
                result ?: return@registerForActivityResult
                viewModel.changeContent(result)
                viewModel.save()
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val sharedIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(sharedIntent)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                editPostLauncher.launch(post.content)
                viewModel.edit(post)
            }

            override fun onVideo(post: Post) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.video)))
            }
        })

        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) {
                    binding.list.scrollToPosition(0)
                }
            }
        }

        val newPostLauncher = registerForActivityResult(NewPostResultContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.changeContent(result)
            viewModel.save()
        }

        binding.fab.setOnClickListener {
            newPostLauncher.launch("")
        }
    }
}

@SuppressLint("DefaultLocale")
fun scaleNumbers(number: String): String {
    var scaledNumber = number
    if (number.toInt() >= 1_000_000) {
        if (number.toInt() / 1_000_000 <= 9) {
            scaledNumber =
                (number.toDouble() / 1_000_000).toBigDecimal().setScale(1, RoundingMode.DOWN)
                    .toString() + "M"
        } else {
            scaledNumber =
                (number.toDouble() / 1_000_000).toBigDecimal().setScale(1, RoundingMode.DOWN)
                    .toInt().toString() + "M"
        }
    } else if (number.toInt() >= 1_000) {
        if (number.toInt() / 1_000 <= 9) {
            scaledNumber = (number.toDouble() / 1_000).toBigDecimal().setScale(1, RoundingMode.DOWN)
                .toString() + "K"
        } else {
            scaledNumber =
                (number.toDouble() / 1_000).toBigDecimal().setScale(1, RoundingMode.DOWN).toInt()
                    .toString() + "K"
        }
    }
    return scaledNumber
}