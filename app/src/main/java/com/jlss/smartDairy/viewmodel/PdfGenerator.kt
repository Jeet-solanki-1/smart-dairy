package com.jlss.smartDairy.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    private val db = AppDatabase.getDatabase(ctx)
    private val userDao = db.userDao()

    fun generateReportPdf(entries: List<Entry>) = viewModelScope.launch(Dispatchers.IO) {
        val user: User? = userDao.getUser().first()

        val pageWidth = 595
        val pageHeight = 842
        val pdf = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        var y = 40f

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val dateFormat = SimpleDateFormat("EEE d MMM yyyy | h:mm a", Locale.getDefault())
        val formattedDateTime = dateFormat.format(Date())
            .replace("AM", "Morning")
            .replace("PM", "Evening")

        canvas.drawText("Smart Dairy Report", 40f, y, paint)
        y += 20f
        canvas.drawText("Date: $formattedDateTime", 40f, y, paint)
        y += 20f
        user?.let {
            canvas.drawText("Name: ${it.name}", 40f, y, paint)
            canvas.drawText("Village: ${it.village}", 200f, y, paint)
            canvas.drawText("Mobile: ${it.mobile}", 400f, y, paint)
            y += 20f
        }

        canvas.drawLine(40f, y, pageWidth - 40f, y, paint)
        y += 20f

        // Table Header
        canvas.drawText("S.No",  40f, y, paint)
        canvas.drawText("Name",  80f, y, paint)
        canvas.drawText("Milk",  220f, y, paint)
        canvas.drawText("Fat",   300f, y, paint)
        canvas.drawText("Amt",   380f, y, paint)
        y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, paint)
        y += 20f

        // Table Rows
        var count = 1
        entries.forEach { e ->
            canvas.drawText("${count++}",           40f, y, paint)
            canvas.drawText(e.name,                80f, y, paint)
            canvas.drawText("%.2f".format(e.milkQty), 220f, y, paint)
            canvas.drawText("%.2f".format(e.fat),     300f, y, paint)
            canvas.drawText("%.2f".format(e.amountToPay), 380f, y, paint)
            y += 20f

            if (y > pageHeight - 120) {
                pdf.finishPage(page)
                // Optional: add code to start a new page if needed.
            }
        }

        // Totals
        y = pageHeight - 100f
        val totalMilk = entries.sumOf { it.milkQty }
        val avgFat = entries.map { it.fat }.average().takeIf { !it.isNaN() } ?: 0.0
        val totalAmt = entries.sumOf { it.amountToPay }

        canvas.drawText("Total Milk: %.2f".format(totalMilk), 40f, y, paint)
        canvas.drawText("Avg Fat:   %.2f".format(avgFat), 200f, y, paint)
        canvas.drawText("Total Pay: %.2f".format(totalAmt), 340f, y, paint)

        // Footer
        y += 40f
        val footerText = "Powered by JLSS"
        val textWidth = paint.measureText(footerText)
        val centeredX = (pageWidth - textWidth) / 2
        canvas.drawText(footerText, centeredX, y, paint)

        pdf.finishPage(page)
        val pdfFile = File(ctx.getExternalFilesDir(null), "report-${System.currentTimeMillis()}.pdf")
        pdf.writeTo(FileOutputStream(pdfFile))
        pdf.close()
        sharePdf(ctx, pdfFile)
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share report via")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
