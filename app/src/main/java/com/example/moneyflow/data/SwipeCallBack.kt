package com.example.moneyflow.data

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R

class SwipeCallback(private val onSwipeLeft: (Int) -> Unit, private val onSwipeRight: (Int) -> Unit) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private var iconDelete: Drawable? = null
    private var iconEdit: Drawable? = null

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        when (direction) {
            ItemTouchHelper.LEFT -> onSwipeLeft(position) // Свайп влево — удаление
            ItemTouchHelper.RIGHT -> onSwipeRight(position) // Свайп вправо — редактирование
        }
    }

    // Метод для отображения иконок при свайпе
    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val iconMargin = (iconDelete?.intrinsicHeight?.let { itemView.height - it } ?: 0) / 2

        // Если свайп влево (удаление)
        if (dX < 0) {
            iconDelete?.setBounds(itemView.right + dX.toInt() - iconMargin, itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)
            iconDelete?.draw(c)
        }

        // Если свайп вправо (редактирование)
        if (dX > 0) {
            iconEdit?.setBounds(itemView.left + iconMargin, itemView.top + iconMargin, itemView.left + iconMargin + (iconEdit?.intrinsicWidth ?: 0), itemView.bottom - iconMargin)
            iconEdit?.draw(c)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Инициализируем иконки, если их еще нет
            if (iconDelete == null) {
                iconDelete = ContextCompat.getDrawable(viewHolder?.itemView?.context!!, R.drawable.ic_delete)
            }
            if (iconEdit == null) {
                iconEdit = ContextCompat.getDrawable(viewHolder?.itemView?.context!!, R.drawable.ic_pencil)
            }
        }
    }
}
