package com.codility.gpslocation

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "customer_table")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val category: String,
    val address: String,
    val numOfSystems: Int,
    val latitude: Double,
    val longitude: Double
    //val date: String,
    //val time: String
): Parcelable