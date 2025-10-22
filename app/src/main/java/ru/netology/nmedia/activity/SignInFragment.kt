package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class SignInFragment : Fragment() {

    val viewModel: PostViewModel by activityViewModels()

    lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(layoutInflater)

        binding.login.setOnClickListener {
            viewModel.signIn(
                login = binding.nameUser.text.toString(),
                pass = binding.passwordUser.text.toString(),
                onSuccess = { findNavController().navigateUp() },
                onFailure = {
                    Snackbar.make(binding.root, R.string.error_login, Snackbar.LENGTH_LONG)
                        .show()
                }
            )

            adapter.refresh()
        }

        binding.cancel.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

}
