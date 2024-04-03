package com.example.cameraxlibrary

import android.app.Activity
import android.content.Intent
import android.net.Uri

object PdfHelper {
    private const val REQUEST_PDF_PICK = 1
    var mimeTypeMain = "pdf"
    fun selectPdfFile(activity: Activity, mimeType: String? = "pdf") {
        mimeTypeMain = mimeType ?: ""
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("application/$mimeType")
        activity.startActivityForResult(intent, REQUEST_PDF_PICK)
    }

    fun handlePdfFileSelection(
        activity: Activity, requestCode: Int, resultCode: Int, data: Intent?
    ) {
        if (requestCode == REQUEST_PDF_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val pdfUri = data.data
                if (pdfUri != null) {
                    displayPdfFile(activity, pdfUri)
                }
            }
        }
    }

    private fun displayPdfFile(activity: Activity, pdfUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/$mimeTypeMain")
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(intent)
    }
}
