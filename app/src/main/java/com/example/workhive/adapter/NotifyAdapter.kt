package com.example.workhive.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.workhive.R
import com.example.workhive.databinding.ItemRemindersBinding
import com.example.workhive.model.Notification
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
class NotifyAdapter(
    private val notifications: List<Notification>
): RecyclerView.Adapter<NotifyAdapter.NotifyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifyViewHolder {
        val binding = ItemRemindersBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return NotifyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotifyViewHolder, position: Int) {
        val notify = notifications[position]
        holder.bind(notify)
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotifyViewHolder(private val binding: ItemRemindersBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(notify: Notification){
            binding.apply {
                notificationMessage.text = notify.content
                notificationTime.text = getTimeAgo(notify.created_at)
                val colorRes = if (notify.is_read == 1) R.color.grey else R.color.white
                root.setBackgroundColor(ContextCompat.getColor(itemView.context, colorRes))
            }
        }

        fun getTimeAgo(timeString: String): String {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            format.timeZone = TimeZone.getTimeZone("UTC")
            return try {
                val past = format.parse(timeString)
                val now = Date()
                val diff = now.time - past.time

                val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                val days = TimeUnit.MILLISECONDS.toDays(diff)

                when {
                    seconds < 60 -> "$seconds giây trước"
                    minutes < 60 -> "$minutes phút trước"
                    hours < 24 -> "$hours giờ trước"
                    else -> "$days ngày trước"
                }
            } catch (e: Exception) {
                timeString // fallback nếu lỗi
            }
        }
    }

}