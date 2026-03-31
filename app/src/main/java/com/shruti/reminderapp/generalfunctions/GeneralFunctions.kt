package com.shruti.reminderapp.generalfunctions

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast

class GeneralFunctions {
    companion object{
        fun dialog(activity: Activity, title : String, subTitle : String){

        }
        fun showToast(context : Context, title : String){
            Toast.makeText(context, title , Toast.LENGTH_SHORT).show()
        }
    }
}