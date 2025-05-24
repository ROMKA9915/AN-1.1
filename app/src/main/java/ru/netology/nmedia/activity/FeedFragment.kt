package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostViewModel
import java.math.RoundingMode

class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

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
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
                viewModel.edit(post)
            }

            override fun onVideo(post: Post) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.video)))
            }

            override fun onSinglePost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_singlePostFragment, Bundle().apply { textArg =post.id.toString() })
            }
        })

        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val newPost = adapter.currentList.size < posts.size
            adapter.submitList(posts) {
                if (newPost) {
                    binding.list.scrollToPosition(0)
                }
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
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