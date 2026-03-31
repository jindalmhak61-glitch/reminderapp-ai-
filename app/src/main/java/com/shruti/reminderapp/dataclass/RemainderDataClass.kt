package com.shruti.reminderapp.dataclass

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class RemainderDataClass(
    var _id : String ?= "" ,
    var title : String ?= "",
    var date : String ?= "" ,
    var time : String ?= ""
)
