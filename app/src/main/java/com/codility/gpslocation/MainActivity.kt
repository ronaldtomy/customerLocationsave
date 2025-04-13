package com.codility.gpslocation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.codility.gpslocation.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val existingCustomer = intent.getParcelableExtra<CustomerEntity>("customer")

        existingCustomer?.let { customer ->
            // Pre-fill fields for editing
            binding.etCustomerName.setText(customer.customerName)
            binding.etCategory.setText(customer.category)
            binding.etAddress.setText(customer.address)
            binding.etNumSystems.setText(customer.numOfSystems.toString())
            binding.tvLatitude.text = customer.latitude.toString()
            binding.tvLongitude.text = customer.longitude.toString()
            currentLatitude = customer.latitude
            currentLongitude = customer.longitude
            binding.btnSaveCustomer.text = "Update Customer"
        }

        binding.btnFetchLocation.setOnClickListener {
            requestLocationPermission()
        }

        binding.btnSaveCustomer.setOnClickListener {
            saveCustomerToDatabase()
        }




    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude

                        binding.tvLatitude.text = location.latitude.toString()
                        binding.tvLongitude.text = location.longitude.toString()
                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCustomerToDatabase() {
        val name = binding.etCustomerName.text.toString()
        val category = binding.etCategory.text.toString()
        val address = binding.etAddress.text.toString()
        val numSystems = binding.etNumSystems.text.toString().toIntOrNull() ?: 0
        val lat = currentLatitude ?: 0.0
        val lng = currentLongitude ?: 0.0
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

        if (name.isEmpty() || category.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val existingCustomer = intent.getParcelableExtra<CustomerEntity>("customer")
        val customer = CustomerEntity(
            id = existingCustomer?.id ?: 0, // If editing, keep same ID
            customerName = name,
            category = category,
            address = address,
            numOfSystems = numSystems,
            latitude = lat,
            longitude = lng,
            //date = currentDate,
            //time = currentTime
        )

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).customerDao()

            if (existingCustomer != null) {
                dao.updateCustomer(customer)
                Toast.makeText(this@MainActivity, "Customer updated", Toast.LENGTH_SHORT).show()
            } else {
                dao.insertCustomer(customer)
                Toast.makeText(this@MainActivity, "Customer added", Toast.LENGTH_SHORT).show()
            }

            clearFields()
            finish() // Go back to dashboard after save
        }
    }

    private fun clearFields() {
        binding.etCustomerName.text.clear()
        binding.etCategory.text.clear()
        binding.etAddress.text.clear()
        binding.etNumSystems.text.clear()
        binding.tvLatitude.text = ""
        binding.tvLongitude.text = ""
        currentLatitude = null
        currentLongitude = null


    }
}
