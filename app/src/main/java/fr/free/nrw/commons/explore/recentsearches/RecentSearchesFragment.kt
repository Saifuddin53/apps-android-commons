package fr.free.nrw.commons.explore.recentsearches

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import fr.free.nrw.commons.R
import fr.free.nrw.commons.databinding.FragmentSearchHistoryBinding
import fr.free.nrw.commons.di.CommonsDaggerSupportFragment
import fr.free.nrw.commons.explore.SearchActivity
import javax.inject.Inject

class RecentSearchesFragment : CommonsDaggerSupportFragment() {

    @Inject lateinit var recentSearchesDao: RecentSearchesDao
    private var recentSearches: List<String> = emptyList()
    private var adapter: ArrayAdapter<String>? = null
    private var _binding: FragmentSearchHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchHistoryBinding.inflate(inflater, container, false)
        recentSearches = recentSearchesDao.recentSearches(10)
        if (recentSearches.isEmpty()) {
            binding.recentSearchesDeleteButton.visibility = View.GONE
            binding.recentSearchesTextView.setText(R.string.no_recent_searches)
        }
        binding.recentSearchesDeleteButton.setOnClickListener { showDeleteRecentAlertDialog(requireContext()) }
        adapter = ArrayAdapter(requireContext(), R.layout.item_recent_searches, recentSearches)
        binding.recentSearchesList.adapter = adapter
        binding.recentSearchesList.setOnItemClickListener { _, _, position, _ ->
            (context as SearchActivity).updateText(recentSearches[position])
        }
        binding.recentSearchesList.setOnItemLongClickListener { _, _, position, _ ->
            showDeleteAlertDialog(requireContext(), position)
            true
        }
        updateRecentSearches()
        return binding.root
    }

    private fun showDeleteRecentAlertDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMessage(getString(R.string.delete_recent_searches_dialog))
            .setPositiveButton(android.R.string.yes) { dialog, _ -> setDeleteRecentPositiveButton(context, dialog) }
            .setNegativeButton(android.R.string.no, null)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun setDeleteRecentPositiveButton(context: Context, dialog: android.content.DialogInterface) {
        recentSearchesDao.deleteAll()
        binding.recentSearchesDeleteButton.visibility = View.GONE
        binding.recentSearchesTextView.setText(R.string.no_recent_searches)
        Toast.makeText(context, getString(R.string.search_history_deleted), Toast.LENGTH_SHORT).show()
        recentSearches = recentSearchesDao.recentSearches(10)
        adapter = ArrayAdapter(context, R.layout.item_recent_searches, recentSearches)
        binding.recentSearchesList.adapter = adapter
        adapter?.notifyDataSetChanged()
        dialog.dismiss()
    }

    private fun showDeleteAlertDialog(context: Context, position: Int) {
        AlertDialog.Builder(context)
            .setMessage(R.string.delete_search_dialog)
            .setPositiveButton(getString(R.string.delete).uppercase()) { dialog, _ ->
                setDeletePositiveButton(context, dialog, position)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setCancelable(false)
            .create()
            .show()
    }

    private fun setDeletePositiveButton(context: Context, dialog: android.content.DialogInterface, position: Int) {
        recentSearchesDao.delete(recentSearchesDao.find(recentSearches[position])!!)
        recentSearches = recentSearchesDao.recentSearches(10)
        adapter = ArrayAdapter(context, R.layout.item_recent_searches, recentSearches)
        binding.recentSearchesList.adapter = adapter
        adapter?.notifyDataSetChanged()
        dialog.dismiss()
    }

    override fun onResume() {
        updateRecentSearches()
        super.onResume()
    }

    fun updateRecentSearches() {
        recentSearches = recentSearchesDao.recentSearches(10)
        adapter?.notifyDataSetChanged()
        if (recentSearches.isNotEmpty()) {
            binding.recentSearchesDeleteButton.visibility = View.VISIBLE
            binding.recentSearchesTextView.setText(R.string.search_recent_header)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
