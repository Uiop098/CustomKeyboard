package com.example.customkeyboard.clipboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customkeyboard.R

class ClipAdapter(
    private val context: Context,
    private var items: List<ClipboardItem>,
    private val onAction: (ClipboardItem, Action) -> Unit
) : RecyclerView.Adapter<ClipAdapter.ClipViewHolder>() {

    enum class Action { PASTE, EDIT, PIN, DELETE, COPY }

    private var filteredItems = items

    fun updateItems(newItems: List<ClipboardItem>) {
        items = newItems
        filteredItems = newItems
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            filteredItems = items
        } else {
            filteredItems = items.filter { item ->
                item.text.lowercase().contains(query.lowercase()) ||
                item.label.lowercase().contains(query.lowercase()) ||
                item.category.lowercase().contains(query.lowercase())
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_clipboard, parent, false)
        return ClipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = filteredItems.size

    inner class ClipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvPreview: TextView = view.findViewById(R.id.tv_preview)
        private val tvMeta: TextView = view.findViewById(R.id.tv_meta)
        private val ivPin: ImageButton = view.findViewById(R.id.iv_pin)
        private val ivMenu: ImageButton = view.findViewById(R.id.iv_menu)
        private val ivCategory: TextView = view.findViewById(R.id.tv_category)

        fun bind(item: ClipboardItem) {
            tvPreview.text = item.getDisplayText()
            
            val timeAgo = getTimeAgo(item.timestamp)
            val pinnedText = if (item.isPinned) " 📌" else ""
            tvMeta.text = "$timeAgo$pinnedText"
            
            ivCategory.text = item.category
            ivPin.setImageResource(if (item.isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin_outline)
            ivPin.setOnClickListener { onAction(item, Action.PIN) }
            
            ivMenu.setOnClickListener { showMenu(it, item) }
            
            // Tap to paste
            itemView.setOnClickListener { onAction(item, Action.PASTE) }
            
            // Long press for menu
            itemView.setOnLongClickListener {
                showMenu(itemView, item)
                true
            }
        }

        private fun showMenu(anchor: View, item: ClipboardItem) {
            val menu = PopupMenu(context, anchor)
            menu.menu.add(0, 0, 0, "Paste").setOnMenuItemClickListener {
                onAction(item, Action.PASTE); true
            }
            menu.menu.add(0, 1, 1, "Edit").setOnMenuItemClickListener {
                onAction(item, Action.EDIT); true
            }
            menu.menu.add(0, 2, 2, if (item.isPinned) "Unpin" else "Pin").setOnMenuItemClickListener {
                onAction(item, Action.PIN); true
            }
            menu.menu.add(0, 3, 3, "Copy to System Clipboard").setOnMenuItemClickListener {
                onAction(item, Action.COPY); true
            }
            menu.menu.add(0, 4, 4, "Delete").setOnMenuItemClickListener {
                onAction(item, Action.DELETE); true
            }
            menu.show()
        }

        private fun getTimeAgo(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            val seconds = diff / 1000
            return when {
                seconds < 60 -> "Just now"
                seconds < 3600 -> "${seconds / 60}m ago"
                seconds < 86400 -> "${seconds / 3600}h ago"
                else -> "${seconds / 86400}d ago"
            }
        }
    }
}