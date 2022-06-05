package id.psw.floatstat.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.psw.floatstat.R
import id.psw.floatstat.select
import java.util.*
import kotlin.collections.ArrayList

class PluginSelectorAdapter(
    private val ctx: Context,
    private val data : ArrayList<PluginSelectorItem>) :
    RecyclerView.Adapter<PluginSelectorAdapter.PluginSelectorViewHolder>() {
    var selectedDefault = -1

    class PluginSelectorViewHolder(itemView: View, private val adapter:PluginSelectorAdapter) : RecyclerView.ViewHolder(itemView) {
        val dataName : TextView = itemView.findViewById(R.id.data_name)
        val pluginName : TextView = itemView.findViewById(R.id.plugin_name)
        val isActive : CheckBox = itemView.findViewById(R.id.is_active)
        val isDefault : RadioButton = itemView.findViewById(R.id.selector_as_default)
        val upButton : Button = itemView.findViewById(R.id.up_button)
        val dnButton : Button = itemView.findViewById(R.id.dn_button)
        var doHandle = true

        private fun swap(up:Boolean, btn:Button){
            val aIdx = bindingAdapterPosition
            val bIdx = aIdx + up.select(-1, 1)
            if(bIdx >= 0 && bIdx < adapter.data.size){
                Collections.swap(adapter.data, aIdx, bIdx)
                adapter.notifyItemChanged(aIdx)
                adapter.notifyItemChanged(bIdx)
            }
        }

        init {
            isActive.setOnCheckedChangeListener { _, isChecked -> adapter.data[position].isActive = isChecked }
            isDefault.setOnCheckedChangeListener { _, _ ->
                if(doHandle){
                    val lastSelectedItem = adapter.selectedDefault
                    adapter.selectedDefault = bindingAdapterPosition
                    adapter.notifyItemChanged(lastSelectedItem)
                    adapter.notifyItemChanged(adapter.selectedDefault)
                }
            }
            upButton.setOnClickListener { swap(true, it as Button) }
            dnButton.setOnClickListener { swap(false,it as Button) }
        }

        fun doButNotHandled(func: (PluginSelectorViewHolder) -> Unit){
            doHandle = false
            func(this)
            doHandle = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PluginSelectorViewHolder {
        val iDat = LayoutInflater.from(ctx).inflate(R.layout.plugin_selector_item, parent, false)

        return PluginSelectorViewHolder(iDat, this)
    }

    override fun onBindViewHolder(holder: PluginSelectorViewHolder, position: Int) {
        holder.dataName.text = data[position].dataName
        holder.pluginName.text = data[position].pluginName
        holder.doButNotHandled {
            it.isActive.isChecked = data[position].isActive
            it.isDefault.isChecked = position == selectedDefault
        }
        holder.upButton.isEnabled = position > 0
        holder.dnButton.isEnabled = position < data.size - 1
    }

    override fun getItemCount(): Int = data.size

}