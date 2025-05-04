package com.example.moneyflow.utils

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.ui.adapters.WalletAdapter

class SwipeCallback(
    private val adapter: WalletAdapter,
    private val onSwipeLeft: (Int) -> Unit,
    private val onSwipeRight: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var iconDelete: Drawable? = null
    private var iconEdit: Drawable? = null

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return when (adapter.getItemViewType(viewHolder.adapterPosition)) {
            WalletAdapter.TYPE_ADD_BUTTON -> {
                0 // Запрещаем свайп для кнопки "Создать новый счет"
            }

            else -> {
                makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            }
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipeLeft(position)
            ItemTouchHelper.RIGHT -> onSwipeRight(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - (iconDelete?.intrinsicHeight ?: 0)) / 2

        if (dX < 0) {
            // Свайп влево — удаление
            val iconTop = itemView.top + iconMargin
            val iconBottom = itemView.bottom - iconMargin
            val iconWidth = iconDelete?.intrinsicWidth ?: 0
            val iconLeft = itemView.right - iconMargin - iconWidth
            val iconRight = itemView.right - iconMargin

            iconDelete?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            iconDelete?.draw(c)
        } else if (dX > 0) {
            // Свайп вправо — редактирование
            val iconTop = itemView.top + iconMargin
            val iconBottom = itemView.bottom - iconMargin
            val iconWidth = iconEdit?.intrinsicWidth ?: 0
            val iconLeft = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + iconWidth

            iconEdit?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            iconEdit?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            viewHolder?.itemView?.context?.let { context ->
                if (iconDelete == null) {
                    iconDelete = ContextCompat.getDrawable(context, R.drawable.ic_delete)?.apply {
                        setTint(ContextCompat.getColor(context, R.color.light_red))
                    }
                }
                if (iconEdit == null) {
                    iconEdit = ContextCompat.getDrawable(context, R.drawable.ic_pencil)?.apply {
                        setTint(ContextCompat.getColor(context, R.color.blue))
                    }
                }
            }
        }
    }
}
