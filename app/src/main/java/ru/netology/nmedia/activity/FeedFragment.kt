package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.NewPostsState
import ru.netology.nmedia.viewmodel.PostViewModel
import java.math.RoundingMode

class FeedFragment : Fragment() {

    private var newPostsSnackbar: Snackbar? = null

    lateinit var binding: FragmentFeedBinding

    lateinit var adapter: PostsAdapter

    val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

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

        adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
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
                    R.id.action_feedFragment_to_singlePostFragment,
                    Bundle().apply { textArg = post.id.toString() })
            }

        })

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.posts)   // Вызывать функцию при нажатии на кнопку к новым постам
            binding.emptyText.isVisible = data.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.newerPost.visibility = View.VISIBLE
            } else {
                binding.newerPost.visibility = View.GONE
            }

            //println(it) // Проверять кол-во новых постов. Если > 0 то показывать кнопку к новым постам, иначе скрывать кнопку
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
            binding.swipeRefresh.isRefreshing = state.refreshing
        }

        binding.newerPost.setOnClickListener {
            viewModel.data.value?.let {
                adapter.submitList(it.posts)
            }

            binding.newerPost.visibility = View.GONE
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeNewPostsState()
        setupSwipeRefresh()
    }

    private fun observeNewPostsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newPostsState.collect { state ->
                when (state) {
                    is NewPostsState.Available -> showNewPostsAvailable(state.count)
                    is NewPostsState.Loaded -> showNewPostsLoaded(state.count)
                    NewPostsState.Hidden -> hideNewPostsIndicator()
                    NewPostsState.Loading -> showNewPostsLoading()
                    NewPostsState.Error -> showNewPostsError()
                }
            }
        }
    }

    private fun showNewPostsAvailable(count: Int) {
        newPostsSnackbar?.dismiss()

        newPostsSnackbar = Snackbar.make(
            binding.root,
            getString(R.string.new_posts_available, count),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.load_new_posts) {
                viewModel.loadNewPosts()
            }
            animationMode = Snackbar.ANIMATION_MODE_SLIDE
            anchorView = binding.fab // Привязываем к FAB если есть
            show()
        }
    }

    private fun showNewPostsLoaded(count: Int) {
        newPostsSnackbar?.dismiss()

        newPostsSnackbar = Snackbar.make(
            binding.root,
            getString(R.string.new_posts_loaded, count),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.show_new_posts) {
                scrollToTopAndShowNewPosts()
            }
            show()
        }
    }

    private fun showNewPostsLoading() {
        newPostsSnackbar?.dismiss()

        newPostsSnackbar = Snackbar.make(
            binding.root,
            getString(R.string.loading_new_posts),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            animationMode = Snackbar.ANIMATION_MODE_FADE
            show()
        }
    }

    private fun showNewPostsError() {
        newPostsSnackbar?.dismiss()

        newPostsSnackbar = Snackbar.make(
            binding.root,
            getString(R.string.new_posts_error),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.retry) {
                viewModel.retryNewPosts()
            }
            show()
        }
    }

    private fun hideNewPostsIndicator() {
        newPostsSnackbar?.dismiss()
        newPostsSnackbar = null
    }

    private fun scrollToTopAndShowNewPosts() {
        // Плавный скролл к верху
        binding.recyclerView.smoothScrollToPosition(0)

        // Показываем новые посты
        viewModel.showNewPosts()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newPostsSnackbar?.dismiss()
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