package com.shruti.reminderapp.interfaces

import com.shruti.reminderapp.dataclass.RemainderDataClass

interface RemainderInterface {
    fun addRemainder(remainderDataClass: RemainderDataClass)
    fun getRemainder(remainderDataClass: RemainderDataClass)
    fun updateRemainder(remainderDataClass: RemainderDataClass)
    fun deleteRemainder(remainderDataClass: RemainderDataClass)
}