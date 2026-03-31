package com.shruti.reminderapp.adapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.shruti.reminderapp.RoomDatabase.AppDatabase
import com.shruti.reminderapp.RoomDatabase.Reminder
import com.shruti.reminderapp.activity.MainActivity
import com.shruti.reminderapp.activity.SetReminderActivity
import com.shruti.reminderapp.databinding.DialogSetReminderBinding
import com.shruti.reminderapp.databinding.ItemRemainderListBinding
import com.shruti.reminderapp.dataclass.Content
import com.shruti.reminderapp.dataclass.RemainderDataClass
import com.shruti.reminderapp.generalfunctions.GeneralFunctions
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RemainderAdapter(var item : ArrayList<Reminder>,context: Context,onClick: OnClick) : RecyclerView.Adapter<RemainderAdapter.ViewHolder>() {
    class ViewHolder(val binding : ItemRemainderListBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRemainderListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.tvTitle.text = item[position].title
        holder.binding.tvDateTime.text = "${item[position].date}"
        holder.binding.tvTime.text="${item[position].time}"



        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SetReminderActivity::class.java)

            intent.putExtra("update", true)
            intent.putExtra("reminder_id", item[position].id)

            context.startActivity(intent)
        }


        holder.binding.delete.setOnClickListener {
            val context = holder.itemView.context
            val reminderId = item[position].id.toString()
            AlertDialog.Builder(context)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Yes") { dialog, _ ->

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }


    }



    interface OnClick {

        fun delete(reminder: Reminder)

    }




}