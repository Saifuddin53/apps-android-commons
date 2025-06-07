package fr.free.nrw.commons.nearby

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import fr.free.nrw.commons.R
import fr.free.nrw.commons.databinding.ItemNearbyFilterLabelBinding
import java.util.ArrayList
import java.util.Locale

class NearbyFilterSearchRecyclerViewAdapter(
    private val labels: List<Label>,
    private val callback: Callback
) : RecyclerView.Adapter<NearbyFilterSearchRecyclerViewAdapter.ViewHolder>(), Filterable {

    private var selectedLabels: ArrayList<Label> = ArrayList()
    private var filteredLabels: ArrayList<Label> = ArrayList(labels)
    private var state: CheckBoxTriStates = CheckBoxTriStates.UNCHECKED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNearbyFilterLabelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = filteredLabels.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredLabels[position])
    }

    inner class ViewHolder(private val binding: ItemNearbyFilterLabelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(label: Label) {
            binding.label = label.text
            binding.checkbox.setOnClickListener {
                label.isSelected = binding.checkbox.isChecked
                if (label.isSelected && !selectedLabels.contains(label)) selectedLabels.add(label)
                callback.filterByMarkerType(selectedLabels, adapterPosition, label.isSelected, true)
            }
        }
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val query = constraint?.toString()?.lowercase(Locale.getDefault()).orEmpty()
            val results = if (query.isEmpty()) labels else labels.filter { it.text.lowercase(Locale.getDefault()).contains(query) }
            return FilterResults().apply { values = ArrayList(results) }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            filteredLabels = results.values as ArrayList<Label>
            notifyDataSetChanged()
        }
    }

    fun selectAll() {
        state = CheckBoxTriStates.CHECKED
        labels.forEach { it.isSelected = true; if (!selectedLabels.contains(it)) selectedLabels.add(it) }
        notifyDataSetChanged()
    }

    interface Callback {
        fun setCheckboxUnknown()
        fun filterByMarkerType(selectedLabels: ArrayList<Label>, i: Int, b: Boolean, b1: Boolean)
        fun isDarkTheme(): Boolean
    }
}

