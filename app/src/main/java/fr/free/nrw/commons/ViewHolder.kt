package fr.free.nrw.commons

import android.content.Context

interface ViewHolder<T> {
    fun bindModel(context: Context, model: T)
}
