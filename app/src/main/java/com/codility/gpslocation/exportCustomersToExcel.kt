import android.content.Context
import com.codility.gpslocation.AppDatabase
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

suspend fun exportCustomersToExcel(context: Context): File? {
    val customers = AppDatabase.getDatabase(context)
        .customerDao()
        .getAllCustomersList()

    val workbook: Workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Customers")

    // Header Row
    val headerRow = sheet.createRow(0)
    val headers = listOf("Name", "Category", "Address", "Systems", "Latitude", "Longitude")
    headers.forEachIndexed { index, title ->
        headerRow.createCell(index).setCellValue(title)
    }

    // Data Rows
    customers.forEachIndexed { index, customer ->
        val row = sheet.createRow(index + 1)
        row.createCell(0).setCellValue(customer.customerName)
        row.createCell(1).setCellValue(customer.category)
        row.createCell(2).setCellValue(customer.address)
        row.createCell(3).setCellValue(customer.numOfSystems.toDouble())
        row.createCell(4).setCellValue(customer.latitude)
        row.createCell(5).setCellValue(customer.longitude)
    }

    return try {
        val fileName = "customer_export_${System.currentTimeMillis()}.xlsx"
        val file = File(context.getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)
        outputStream.close()
        workbook.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
