package fr.free.nrw.commons.bookmarks.items

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import fr.free.nrw.commons.R
import fr.free.nrw.commons.databinding.FragmentBookmarksItemsBinding
import fr.free.nrw.commons.upload.structure.depictions.DepictedItem
import javax.inject.Inject

class BookmarkItemsFragment : DaggerFragment() {

    private var _binding: FragmentBookmarksItemsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var controller: BookmarkItemsController

    companion object {
        fun newInstance() = BookmarkItemsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList(requireContext())
    }

    override fun onResume() {
        super.onResume()
        initList(requireContext())
    }

    private fun initList(context: Context) {
        val depictItems: List<DepictedItem> = controller.loadFavoritesItems()
        val adapter = BookmarkItemsAdapter(depictItems, context)
        binding.listView.adapter = adapter
        binding.loadingImagesProgressBar.visibility = View.GONE
        if (depictItems.isEmpty()) {
            binding.statusMessage.setText(R.string.bookmark_empty)
            binding.statusMessage.visibility = View.VISIBLE
        } else {
            binding.statusMessage.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

