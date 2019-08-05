package com.example.zcrmcptestapp

import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class RecyclerViewAdapter internal constructor() : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    private lateinit var names: ArrayList<String?>
    private lateinit var onItemClickListener: OnItemClickListener

    constructor(names: ArrayList<String?>, onItemClickListener: OnItemClickListener): this() {
        this.names = names
        this.onItemClickListener = onItemClickListener
    }

    class MyViewHolder internal constructor(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        lateinit var title: TextView
        lateinit var onItemClickListener: OnItemClickListener

        constructor(view: View, onItemClickListener: OnItemClickListener): this(view) {
            this.title = view as TextView
            this.onItemClickListener = onItemClickListener
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            onItemClickListener.onClick(adapterPosition)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val rowLayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)

//        val rowLayout = LinearLayout(parent.context)
//        rowLayout.orientation = LinearLayout.VERTICAL
//        rowLayout.layoutParams = rowLayoutParams

        val txtView = TextView(parent.context)
        txtView.layoutParams = rowLayoutParams
        txtView.gravity = Gravity.START
        txtView.textSize = 22F

        return MyViewHolder(txtView, onItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val name = names[position]
        holder.title.text = name
    }

    override fun getItemCount() = names.size

    interface OnItemClickListener {
        fun onClick(position: Int)
    }
}
