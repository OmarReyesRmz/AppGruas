package com.example.gruas

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GruaSpinnerAdapter(
    context: Context,
    private val gruas: List<Gruas>
) : ArrayAdapter<Gruas>(context, android.R.layout.simple_spinner_item, gruas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = gruas[position].tipo_grua
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = gruas[position].tipo_grua
        return view
    }
}