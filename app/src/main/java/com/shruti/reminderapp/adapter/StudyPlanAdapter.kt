package com.shruti.reminderapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shruti.reminderapp.activity.StudyPlan
import com.shruti.reminderapp.databinding.ItemStudyPlanBinding

class StudyPlanAdapter(private val list: List<StudyPlan>) :
    RecyclerView.Adapter<StudyPlanAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStudyPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudyPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = list[position]
        holder.binding.apply {
            tvDate.text = plan.date
            tvPlanTitle.text = "${plan.days} Days Study Plan"
            tvContentPreview.text = plan.content

            root.setOnClickListener {
                // Show Alert Dialog with Full Plan
                MaterialAlertDialogBuilder(root.context)
                    .setTitle("${plan.days} Days Plan Details")
                    .setMessage(plan.content)
                    .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = list.size
}