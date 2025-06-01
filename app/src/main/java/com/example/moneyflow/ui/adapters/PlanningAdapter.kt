package com.example.moneyflow.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyflow.R
import com.example.moneyflow.data.Plan
import com.example.moneyflow.utils.Formatter.formatWithSpaces
import com.google.android.material.materialswitch.MaterialSwitch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlanningAdapter(
    private val onAddClick: () -> Unit,
    private val onNotificationToggle: (Plan, Boolean) -> Unit,
    private val onItemClick: (Plan) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var plans = listOf<Plan>()
        private set

    companion object {
        private const val TYPE_PLAN = 0
        private const val TYPE_ADD_BUTTON = 1
    }

    fun updatePlans(newPlans: List<Plan>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = plans.size
            override fun getNewListSize() = newPlans.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return plans[oldItemPosition].id == newPlans[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return plans[oldItemPosition] == newPlans[newItemPosition]
            }
        })

        plans = newPlans
        diffResult.dispatchUpdatesTo(this)
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
            val view = inflater.inflate(R.layout.wallet_item, parent, false)
            AddButtonViewHolder(view)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val context = holder.itemView.context
        if (holder is PlansViewHolder) {
            val plan = plans[position]
            val formattedSum = plan.sum.formatWithSpaces(holder.itemView.context)
            holder.textViewPlanName.text =
                holder.itemView.context.getString(R.string.plan_name_sum, plan.name, formattedSum)

            holder.switchNotification.setOnCheckedChangeListener(null)
            holder.switchNotification.isChecked = plan.isNotificationActive
            holder.switchNotification.setOnCheckedChangeListener { _, isChecked ->
                onNotificationToggle(plan, isChecked)
            }

            val targetDay = plan.targetNotificationDayOfMonth
            val currentDate = LocalDate.now()
            val currentMonth = currentDate.monthValue
            val currentYear = currentDate.year

            val nextPaymentDate: LocalDate = if (currentDate.dayOfMonth <= targetDay) {
                LocalDate.of(currentYear, currentMonth, targetDay)
            } else {
                if (currentMonth == 12) {
                    LocalDate.of(currentYear + 1, 1, targetDay)
                } else {
                    LocalDate.of(currentYear, currentMonth + 1, targetDay)
                }
            }

            val formatter = DateTimeFormatter.ofPattern("d MMMM", java.util.Locale.getDefault())
            val formattedDate = nextPaymentDate.format(formatter)

            holder.textViewNextPaymentDate.text =
                holder.itemView.context.getString(R.string.next_payment, formattedDate)

            holder.itemView.setOnClickListener {
                onItemClick(plan)
            }

        } else if (holder is AddButtonViewHolder) {
            holder.textViewBalance.visibility = View.GONE

            val params = holder.textViewName.layoutParams as LinearLayout.LayoutParams
            params.height = LinearLayout.LayoutParams.MATCH_PARENT
            params.weight = 0f
            holder.textViewName.layoutParams = params
            holder.textViewName.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.textViewName.gravity = android.view.Gravity.CENTER_VERTICAL

            holder.textViewName.text = context.getString(R.string.create_new_plan)
            holder.imageViewIcon.setImageResource(R.drawable.ic_add)

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
        val switchNotification: MaterialSwitch = itemView.findViewById(R.id.switchNotification)
    }

    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewWalletIcon)
        val textViewName: TextView = itemView.findViewById(R.id.textViewWalletName)
        val textViewBalance: TextView = itemView.findViewById(R.id.textViewWalletBalance)
    }
}