package fr.free.nrw.commons.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import fr.free.nrw.commons.databinding.FragmentMediaDetailPagerBinding
import fr.free.nrw.commons.media.page.MediaDetailFragment

class MediaDetailPagerFragment : Fragment() {

    private var _binding: FragmentMediaDetailPagerBinding? = null
    private val binding get() = _binding!!

    private val adapter by lazy {
        object : FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment = MediaDetailFragment.newInstance(position)
            override fun getCount(): Int = items.size
        }
    }

    var items: List<Media> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaDetailPagerBinding.inflate(inflater, container, false)
        binding.viewPager.adapter = adapter
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showImage(index: Int) {
        binding.viewPager.currentItem = index
    }

    fun getMediaAtPosition(position: Int): Media? = items.getOrNull(position)

    companion object {
        fun newInstance(displayShareButton: Boolean, usePager: Boolean): MediaDetailPagerFragment {
            return MediaDetailPagerFragment()
        }
    }
}

