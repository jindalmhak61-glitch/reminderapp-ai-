package com.shruti.reminderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shruti.reminderapp.R
import com.shruti.reminderapp.activity.User

class UserStatsAdapter(private val list: List<UserStats>) :
    RecyclerView.Adapter<UserStatsAdapter.StatsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_stats_combined, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.user.name
        holder.email.text = item.user.email
        holder.remCount.text = "Total Reminders: ${item.reminderCount}"
        holder.planCount.text = "AI Study Plans: ${item.planCount}"
    }

    override fun getItemCount() = list.size

    class StatsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvStatName)
        val email: TextView = v.findViewById(R.id.tvStatEmail)
        val remCount: TextView = v.findViewById(R.id.tvStatRemCount)
        val planCount: TextView = v.findViewById(R.id.tvStatPlanCount)
    }
}

data class UserStats(
    val user: User,
    val reminderCount: Int,
    val planCount: Int
)