package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FullscreenImageViewBinding
import ru.netology.nmedia.utils.StringArg

class FullScreenImageFragment : Fragment() {

    lateinit var binding: FullscreenImageViewBinding

//    init {
//        println("890")
//    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }

//    private val viewModel: FullScreenImageFragment by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FullscreenImageViewBinding.inflate(
            inflater,
            container,
            false
        )

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(binding.fullscreenImageView)
            .load(arguments?.textArg)
            .into(binding.fullscreenImageView)

        binding.closeButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}
