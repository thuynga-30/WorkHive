package com.example.workhive.helper

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.workhive.R
import com.example.workhive.view.DetailGroupActivity
import com.example.workhive.view.TeamActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {
    fun setupBottom(activity: Activity, selectedItemId: Int){
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId =selectedItemId
        bottomNav.setOnItemSelectedListener {
            item -> when( item.itemId){
                R.id.menu_home ->{
                    if (activity !is TeamActivity){
                        activity.startActivity(Intent(activity, TeamActivity::class.java))
                        activity.finish()
                    }
                    true
                }
            R.id.menu_dashboard ->{
                Toast.makeText(activity, "Thống kê", Toast.LENGTH_SHORT).show()

                true
            }
            R.id.menu_chat -> {
//                if (activity !is DetailGroupActivity) {
//                    activity.startActivity(Intent(activity, DetailGroupActivity::class.java))
//                    activity.finish()
//                }
                Toast.makeText(activity, "Chat", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_reminders -> {
                Toast.makeText(activity, "Thông Báo", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_teams -> {
                Toast.makeText(activity, "Thống kê", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
            }
        }
    }
