package com.codility.gpslocation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codility.gpslocation.databinding.DashboardBinding
import kotlinx.coroutines.launch
import java.io.File
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import exportCustomersToExcel
import exportCustomersToPdf

class DashBoardActivity : AppCompatActivity() {
    private lateinit var customerAdapter: CustomerAdapter
    private lateinit var binding:DashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.fabAddCustomer.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

       /* binding.btnExportExcel.setOnClickListener {
            lifecycleScope.launch {
                val file = exportCustomersToExcel(applicationContext)
                if (file != null) {
                    Toast.makeText(this@DashBoardActivity, "Excel saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DashBoardActivity, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }*/

        binding.btnClearAll.setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.getDatabase(applicationContext)
                    .customerDao()
                    .deleteAllCustomers()

                Toast.makeText(this@DashBoardActivity, "All customers deleted", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnExportAndShareExcel.setOnClickListener {
            lifecycleScope.launch {
                val file = exportCustomersToExcel(applicationContext)
                if (file != null) {
                    shareExcelFile(this@DashBoardActivity, file)
                } else {
                    Toast.makeText(this@DashBoardActivity, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadCustomers()

        binding.btnImportBackup.setOnClickListener {
            importBackupLauncher.launch("application/json")
        }

        binding.btnBackupData.setOnClickListener {
            lifecycleScope.launch {
                val json = exportCustomersAsJson(applicationContext)
                val file = saveJsonToFile(applicationContext, json)
                if (file != null) {
                    Toast.makeText(this@DashBoardActivity, "Backup saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DashBoardActivity, "Backup failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnExportPdf.setOnClickListener {
            lifecycleScope.launch {
                val file = exportCustomersToPdf(applicationContext)
                if (file != null) {
                    sharePdfFile(this@DashBoardActivity, file)
                } else {
                    Toast.makeText(this@DashBoardActivity, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCustomers()
    }

    private fun loadCustomers() {
        // Observe the LiveData from the DAO
        AppDatabase.getDatabase(applicationContext)
            .customerDao()
            .getAllCustomers()
            .observe(this, { customers ->
                // Update the adapter when data is changed
                customerAdapter = CustomerAdapter(customers) { selectedCustomer ->
                    val intent = Intent(this@DashBoardActivity, MainActivity::class.java)
                    intent.putExtra("customer", selectedCustomer)
                    startActivity(intent)
                }
                binding.recyclerView.adapter = customerAdapter
            })
    }

    suspend fun exportCustomersAsJson(context: Context): String {
        val customers = AppDatabase.getDatabase(context).customerDao().getAllCustomersList()

        val dtoList = customers.map {
            CustomerDTO(
                customerName = it.customerName,
                category = it.category,
                address = it.address,
                numOfSystems = it.numOfSystems,
                latitude = it.latitude,
                longitude = it.longitude,
                //date = it.date,
                //time = it.time
            )
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(dtoList)
    }

    private fun shareExcelFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",  // e.g., com.codility.gpslocation.provider
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            //setPackage("com.whatsapp") // force open in WhatsApp only
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Excel via"))
    }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val json = readJsonFromUri(it)
                val customerList = importCustomersFromJson(json)
                saveCustomersToDatabase(customerList)
            }
        }
    }

    fun saveJsonToFile(context: Context, jsonData: String): File? {
        return try {
            val fileName = "customer_backup_${System.currentTimeMillis()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            file.writeText(jsonData)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importCustomersFromJson(json: String): List<CustomerEntity> {
        val type = object : TypeToken<List<CustomerDTO>>() {}.type
        val dtoList: List<CustomerDTO> = Gson().fromJson(json, type)

        return dtoList.map {
            CustomerEntity(
                customerName = it.customerName,
                category = it.category,
                address = it.address,
                numOfSystems = it.numOfSystems,
                latitude = it.latitude,
                longitude = it.longitude
                //date = it.date,
                //time = it.time
            )
        }
    }

    private fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            //setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share PDF via"))
    }

    private suspend fun readJsonFromUri(uri: Uri): String {
        return contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
    }

    private suspend fun saveCustomersToDatabase(customers: List<CustomerEntity>) {
        val dao = AppDatabase.getDatabase(applicationContext).customerDao()
        customers.forEach { dao.insertCustomer(it) }

        Toast.makeText(this, "Imported ${customers.size} customers", Toast.LENGTH_SHORT).show()
    }
}