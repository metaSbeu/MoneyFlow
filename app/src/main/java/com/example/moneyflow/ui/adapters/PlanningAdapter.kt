package com.example.moneyflow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Plan
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlanningAdapter(
    private val onAddClick: () -> Unit,
    private val onNotificationToggle: (Plan, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var plans = listOf<Plan>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        private const val TYPE_PLAN = 0
        private const val TYPE_ADD_BUTTON = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < plans.size) TYPE_PLAN else TYPE_ADD_BUTTON
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_PLAN) {
            val view = inflater.inflate(R.layout.plan_item, parent, false)
            PlansViewHolder(view)
        } else {
            val view =
                inflater.inflate(R.layout.wallet_item, parent, false) // Используем правильный макет
            AddButtonViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (holder is PlansViewHolder) {
            val plan = plans[position]
            holder.textViewPlanName.text = holder.itemView.context.getString(R.string.plan_name_sum, plan.name, plan.sum)
            holder.switchNotification.isChecked = plan.isNotificationActive

            val targetDay = plan.targetNotificationDayOfMonth
            val currentDate = LocalDate.now()
            val currentMonth = currentDate.monthValue
            val currentYear = currentDate.year

            val nextPaymentDate: LocalDate = if (currentDate.dayOfMonth <= targetDay) {
                LocalDate.of(currentYear, currentMonth, targetDay)
            } else {
                LocalDate.of(currentYear, currentMonth + 1, targetDay)
            }

            val formatter = DateTimeFormatter.ofPattern("d MMMM", java.util.Locale.getDefault())
            val formattedDate = nextPaymentDate.format(formatter)

            holder.textViewNextPaymentDate.text =
                holder.itemView.context.getString(R.string.next_payment, formattedDate)

            holder.switchNotification.setOnCheckedChangeListener { _, isChecked ->
                onNotificationToggle(plan, isChecked)
            }

        } else if (holder is AddButtonViewHolder) {
            holder.textViewName.text = "Создать новый план"
            holder.imageViewIcon.setImageResource(R.drawable.ic_add_black)
            holder.itemView.setBackgroundResource(R.drawable.bg_wallet_normal)
            holder.itemView.setOnClickListener {
                onAddClick()
            }
        }
    }

    override fun getItemCount(): Int {
        return plans.size + 1
    }

    class PlansViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewPlanName: TextView = itemView.findViewById(R.id.textViewPlanName)
        val textViewNextPaymentDate: TextView = itemView.findViewById(R.id.textViewNextPaymentDate)
        val switchNotification: SwitchCompat = itemView.findViewById(R.id.switchNotification)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewWalletIcon)
        val textViewName: TextView = itemView.findViewById(R.id.textViewWalletName)
    }
}