package fr.free.nrw.commons.bookmarks.pictures

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListAdapter
import androidx.fragment.app.Fragment
import dagger.android.support.DaggerFragment
import fr.free.nrw.commons.Media
import fr.free.nrw.commons.R
import fr.free.nrw.commons.bookmarks.BookmarkListRootFragment
import fr.free.nrw.commons.category.GridViewAdapter
import fr.free.nrw.commons.databinding.FragmentBookmarksPicturesBinding
import fr.free.nrw.commons.utils.NetworkUtils
import fr.free.nrw.commons.utils.ViewUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class BookmarkPicturesFragment : DaggerFragment() {

    private var gridAdapter: GridViewAdapter? = null
    private val compositeDisposable = CompositeDisposable()
    private var _binding: FragmentBookmarksPicturesBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var controller: BookmarkPicturesController

    companion object {
        fun newInstance() = BookmarkPicturesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarksPicturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bookmarkedPicturesList.onItemClickListener =
            getParentFragment() as AdapterView.OnItemClickListener
        initList()
    }

    override fun onStop() {
        super.onStop()
        controller.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (controller.needRefreshBookmarkedPictures()) {
            binding.bookmarkedPicturesList.visibility = View.GONE
            gridAdapter?.clear()
            (parentFragment as BookmarkListRootFragment).viewPagerNotifyDataSetChanged()
            initList()
        }
    }

    @SuppressLint("CheckResult")
    private fun initList() {
        if (!NetworkUtils.isInternetConnectionEstablished(context)) {
            handleNoInternet()
            return
        }
        binding.loadingImagesProgressBar.visibility = View.VISIBLE
        binding.statusMessage.visibility = View.GONE
        compositeDisposable.add(
            controller.loadBookmarkedPictures()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ handleSuccess(it) }, { handleError(it) })
        )
    }

    private fun handleNoInternet() {
        binding.loadingImagesProgressBar.visibility = View.GONE
        if (gridAdapter == null || gridAdapter!!.isEmpty) {
            binding.statusMessage.visibility = View.VISIBLE
            binding.statusMessage.text = getString(R.string.no_internet)
        } else {
            ViewUtil.showShortSnackbar(binding.parentLayout, R.string.no_internet)
        }
    }

    private fun handleError(throwable: Throwable) {
        Timber.e(throwable, "Error occurred while loading images inside a category")
        try {
            ViewUtil.showShortSnackbar(binding.root, R.string.error_loading_images)
            initErrorView()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initErrorView() {
        binding.loadingImagesProgressBar.visibility = View.GONE
        if (gridAdapter == null || gridAdapter!!.isEmpty) {
            binding.statusMessage.visibility = View.VISIBLE
            binding.statusMessage.text = getString(R.string.no_images_found)
        } else {
            binding.statusMessage.visibility = View.GONE
        }
    }

    private fun initEmptyBookmarkListView() {
        binding.loadingImagesProgressBar.visibility = View.GONE
        if (gridAdapter == null || gridAdapter!!.isEmpty) {
            binding.statusMessage.visibility = View.VISIBLE
            binding.statusMessage.text = getString(R.string.bookmark_empty)
        } else {
            binding.statusMessage.visibility = View.GONE
        }
    }

    private fun handleSuccess(collection: List<Media>?) {
        if (collection == null) {
            initErrorView()
            return
        }
        if (collection.isEmpty()) {
            initEmptyBookmarkListView()
            return
        }
        if (gridAdapter == null) {
            setAdapter(collection)
        } else {
            if (gridAdapter!!.containsAll(collection)) {
                binding.loadingImagesProgressBar.visibility = View.GONE
                binding.statusMessage.visibility = View.GONE
                binding.bookmarkedPicturesList.visibility = View.VISIBLE
                binding.bookmarkedPicturesList.adapter = gridAdapter
                return
            }
            gridAdapter!!.addItems(collection)
            (parentFragment as BookmarkListRootFragment).viewPagerNotifyDataSetChanged()
        }
        binding.loadingImagesProgressBar.visibility = View.GONE
        binding.statusMessage.visibility = View.GONE
        binding.bookmarkedPicturesList.visibility = View.VISIBLE
    }

    private fun setAdapter(mediaList: List<Media>) {
        gridAdapter = GridViewAdapter(requireContext(), R.layout.layout_category_images, mediaList)
        binding.bookmarkedPicturesList.adapter = gridAdapter
    }

    fun getAdapter(): ListAdapter = binding.bookmarkedPicturesList.adapter
}

