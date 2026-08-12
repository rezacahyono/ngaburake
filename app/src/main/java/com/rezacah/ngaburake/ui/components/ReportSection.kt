package com.rezacah.ngaburake.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rezacah.ngaburake.report.ReportFormat

/** Segmented control for choosing the report format (JSON / HTML / CONSOLE). */
@Composable
fun FormatSelector(
    selected: ReportFormat,
    onSelect: (ReportFormat) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ReportFormat.entries.forEachIndexed { index, format ->
            SegmentedButton(
                selected = selected == format,
                onClick = { onSelect(format) },
                shape = SegmentedButtonDefaults.itemShape(index, ReportFormat.entries.size),
                label = { Text(format.name) },
            )
        }
    }
}

@Composable
fun ReportPreviewSection(
    format: ReportFormat,
    reportText: String,
    onCopy: (String) -> Unit,
) {
    when (format) {
        ReportFormat.HTML -> HtmlReportView(html = reportText)
        else -> PlainTextReportView(text = reportText, onCopy = onCopy)
    }
}

/** Copy-to-clipboard helper used by the plain-text preview. */
@Composable
fun rememberCopyAction(): (String) -> Unit {
    val clipboard = LocalClipboardManager.current
    return { text -> clipboard.setText(AnnotatedString(text)) }
}

@Composable
private fun PlainTextReportView(text: String, onCopy: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 4.dp, end = 4.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onCopy(text) }) {
                Icon(
                    Icons.Filled.ContentCopy,
                    contentDescription = "Copy report",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        )
    }
}

/**
 * Renders the HTML report in a WebView with a fixed height and its own internal scroll.
 *
 * The touch listener claims vertical drags from the parent LazyColumn
 * (`requestDisallowInterceptTouchEvent`), which is what lets the WebView scroll its own content
 * instead of the LazyColumn stealing the gesture.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun HtmlReportView(html: String) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = false // static report, no JS needed
            setOnTouchListener { _, _ ->
                parent?.requestDisallowInterceptTouchEvent(true)
                false // keep WebView's own touch handling
            }
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        AndroidView(
            factory = { webView },
            update = { it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
        )
    }
}
