package ru.netology.nmedia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.repository.PostViewModel
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                avatar.setImageResource(post.avatar)

                if (post.likedByMe) likes.setImageResource(R.drawable.ic_liked_24) else likes.setImageResource(R.drawable.ic_like_24)

                likeCount.text = scaleNumbers(post.likes.toString())
                shareCount.text = scaleNumbers(post.shares.toString())
                viewsCount.text = scaleNumbers(post.views.toString())
            }

            binding.likes.setOnClickListener {
                if(post.likedByMe) {
                    viewModel.disliked()
                    binding.likeCount.text = scaleNumbers(post.likes.toString())
                } else {
                    viewModel.liked()
                    binding.likeCount.text = scaleNumbers(post.likes.toString())
                }
                viewModel.like()
            }

            binding.likes.setImageResource(
                if (post.likedByMe) {
                    R.drawable.ic_liked_24
                } else {
                    R.drawable.ic_like_24
                }
            )

            binding.shares.setOnClickListener {
                viewModel.shared()
                binding.shareCount.text = scaleNumbers(post.shares.toString())
            }
        }

    }
}

@SuppressLint("DefaultLocale")
fun scaleNumbers(number: String): String {
    var scaledNumber = number
    if (number.toInt() >= 1_000_000) {
        if (number.toInt()/1_000_000 <= 9) {
            scaledNumber = (number.toDouble() / 1_000_000).toBigDecimal().setScale(1, RoundingMode.DOWN).toString() + "M"
        } else {
            scaledNumber = (number.toDouble() / 1_000_000).toBigDecimal().setScale(1, RoundingMode.DOWN).toInt().toString() + "M"
        }
    } else if (number.toInt() >= 1_000) {
        if (number.toInt()/1_000 <= 9) {
            scaledNumber = (number.toDouble() / 1_000).toBigDecimal().setScale(1, RoundingMode.DOWN).toString() + "K"
        } else {
            scaledNumber = (number.toDouble() / 1_000).toBigDecimal().setScale(1, RoundingMode.DOWN).toInt().toString() + "K"
        }
    }
    return scaledNumber
}