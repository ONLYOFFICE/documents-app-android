package lib.toolkit.base.managers.print

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import lib.toolkit.base.managers.utils.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class PrintPdfAdapter(val context: Context,
                      private val pathFile: String,
                      private val finishPrint: () -> Unit) : PrintDocumentAdapter() {

    override fun onLayout(oldAttributes: PrintAttributes,
                          newAttributes: PrintAttributes,
                          cancellationSignal: CancellationSignal?,
                          callback: LayoutResultCallback,
                          extras: Bundle?) {

        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        PrintDocumentInfo.Builder(FileUtils.getFileName(pathFile, true))
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build()
                .also { info ->
                    callback.onLayoutFinished(info, newAttributes != oldAttributes)
                }

    }

    override fun onWrite(pages: Array<out PageRange>,
                         destination: ParcelFileDescriptor,
                         cancellationSignal: CancellationSignal,
                         callback: WriteResultCallback) {
        try {

            cancellationSignal.setOnCancelListener { finishPrint.invoke() }

            FileInputStream(File(pathFile)).use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    output.write(input.readBytes())
                }
            }
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        }

        if (cancellationSignal.isCanceled) {
            callback.onWriteCancelled()
        } else {
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }

    }

    override fun onFinish() {
        finishPrint.invoke()
    }

}