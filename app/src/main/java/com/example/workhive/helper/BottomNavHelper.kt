package com.example.workhive.helper

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.example.workhive.R
import com.example.workhive.view.DetailGroupActivity
import com.example.workhive.view.HomeActivity
import com.example.workhive.view.LoginActivity
import com.example.workhive.view.NotifyActivity
import com.example.workhive.view.TeamActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object BottomNavHelper {
    fun setupBottom(activity: Activity, selectedItemId: Int){
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId =selectedItemId

        bottomNav.setOnItemSelectedListener {
            item -> when( item.itemId){
                R.id.menu_home ->{
                    if (activity !is HomeActivity){
                        activity.startActivity(Intent(activity, HomeActivity::class.java))
                        activity.finish()
                    }
                    true
                }
            R.id.menu_exit ->{
                val sharedPref = activity.getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                sharedPref.edit().clear().apply()
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
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
                if (activity !is NotifyActivity){
                    activity.startActivity(Intent(activity, NotifyActivity::class.java))
                    activity.finish()
                }
                true
            }
            R.id.menu_teams -> {
                if (activity !is TeamActivity){
                    activity.startActivity(Intent(activity, TeamActivity::class.java))
                    activity.finish()
                }
                true
            }
            else -> false
        }
            }
        }
    }
