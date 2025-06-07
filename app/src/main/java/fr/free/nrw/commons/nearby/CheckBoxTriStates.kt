package fr.free.nrw.commons.nearby

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatCheckBox
import fr.free.nrw.commons.R

class CheckBoxTriStates @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    interface Callback {
        fun filterByMarkerType(selectedLabels: List<Label>?, state: Int, b: Boolean, b1: Boolean)
    }

    var state = UNKNOWN
        private set

    var callback: Callback? = null

    private val privateListener = CompoundButton.OnCheckedChangeListener { _, _ ->
        when (state) {
            UNKNOWN -> setState(UNCHECKED)
            UNCHECKED -> setState(CHECKED)
            CHECKED -> setState(UNKNOWN)
        }
    }

    private var clientListener: OnCheckedChangeListener? = null

    fun setState(state: Int) {
        if (this.state != state) {
            this.state = state
            clientListener?.onCheckedChanged(this, isChecked)
            if (NearbyController.currentLocation != null) {
                callback?.filterByMarkerType(null, state, false, true)
            }
            updateBtn()
        }
    }

    override fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        if (privateListener != listener) {
            clientListener = listener
        }
        super.setOnCheckedChangeListener(privateListener)
    }

    fun addAction() {
        setOnCheckedChangeListener(privateListener)
    }

    private fun updateBtn() {
        val btnDrawable = when (state) {
            UNKNOWN -> R.drawable.ic_indeterminate_check_box_black_24dp
            UNCHECKED -> R.drawable.ic_check_box_outline_blank_black_24dp
            CHECKED -> R.drawable.ic_check_box_black_24dp
            else -> R.drawable.ic_indeterminate_check_box_black_24dp
        }
        setButtonDrawable(btnDrawable)
    }

    companion object {
        const val UNKNOWN = -1
        const val UNCHECKED = 0
        const val CHECKED = 1
    }
}
