package com.codility.gpslocation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface CustomerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customer_table")
    fun getAllCustomers(): LiveData<List<CustomerEntity>>

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customer_table")
    suspend fun getAllCustomersList(): List<CustomerEntity>

    @Query("DELETE FROM customer_table")
    suspend fun deleteAllCustomers()
}