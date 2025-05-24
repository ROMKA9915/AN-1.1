package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.repository.PostViewModel
import ru.netology.nmedia.utils.StringArg

class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        binding.editText.requestFocus()

        arguments?.textArg?.let(binding.editText::setText)

        binding.ok.setOnClickListener {

            if (binding.editText.text.isNotBlank()) {
                val content = binding.editText.text.toString()
                viewModel.changeContent(content)
                viewModel.save()
            }
            findNavController().navigateUp()
        }
        return binding.root
    }

    companion object {
        var Bundle.textArg by StringArg
    }
}