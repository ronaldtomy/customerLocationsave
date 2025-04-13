import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.codility.gpslocation.AppDatabase
import java.io.File
import java.io.FileOutputStream

suspend fun exportCustomersToPdf(context: Context): File? {
    val customers = AppDatabase.getDatabase(context)
        .customerDao()
        .getAllCustomersList()

    val pdfDocument = PdfDocument()
    val paint = Paint()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    var y = 50
    paint.textSize = 14f
    canvas.drawText("Customer List", 200f, y.toFloat(), paint)
    y += 30

    customers.forEachIndexed { index, c ->
        canvas.drawText("${index + 1}. ${c.customerName}, ${c.category}, ${c.address}", 10f, y.toFloat(), paint)
        y += 20
    }

    pdfDocument.finishPage(page)

    val fileName = "customer_list_${System.currentTimeMillis()}.pdf"
    val file = File(context.getExternalFilesDir(null), fileName)

    return try {
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}