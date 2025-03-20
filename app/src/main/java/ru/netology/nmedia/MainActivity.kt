package ru.netology.nmedia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
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

        val post = Post(
            1,
            "Нетология. Университет интернет-профессий будущего",
            "21 мая в 18:36",
            "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            false
        )

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = post.likes.toString()
            shareCount.text = post.shares.toString()
            viewsCount.text = post.views.toString()

            likes.setImageResource(R.drawable.ic_like_24)
            likeCount.text = scaleNumbers(likeCount.text.toString())
            shareCount.text = scaleNumbers(shareCount.text.toString())

            likes.setOnClickListener {
                post.likedByMe = !post.likedByMe
                println("лайк")

                likes.setImageResource(
                    if (post.likedByMe) {
                        post.likes++
                        likeCount.text = post.likes.toString()
                        likeCount.text = scaleNumbers(likeCount.text.toString())
                        R.drawable.ic_liked_24
                    } else {
                        post.likes--
                        likeCount.text = post.likes.toString()
                        likeCount.text = scaleNumbers(likeCount.text.toString())
                        R.drawable.ic_like_24
                    }
                )
            }

            shares.setOnClickListener {
                post.shares ++
                shareCount.text = post.shares.toString()
                shareCount.text = scaleNumbers(shareCount.text.toString())
                println("поделиться")
            }

            root.setOnClickListener {
                post.shares ++
                viewsCount.text = post.shares.toString()
                println("рут")
            }


        }

    }
}

@SuppressLint("DefaultLocale")
fun scaleNumbers(number: String): String {
    var scaledNumber = number
    if (number.toInt() >= 1_000_000) {
        if (number.toInt()/1_000_000 <= 9) {
            scaledNumber = String.format("%.1f", (number.toDouble() / 1_000_000)).toString() + "M"
        } else {
            scaledNumber = (number.toInt() / 1_000_000).toString() + "M"
        }
    } else if (number.toInt() >= 1_000) {
        if (number.toInt()/1_000 <= 9) {
            scaledNumber = String.format("%.1f", (number.toDouble() / 1_000)).toString() + "K"
        } else {
            scaledNumber = (number.toInt() / 1_000).toString() + "K"
        }
    }
    return scaledNumber
}