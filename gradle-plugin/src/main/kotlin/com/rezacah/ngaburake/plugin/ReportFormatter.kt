package com.rezacah.ngaburake.plugin

/** Renders a list of [Finding]s into a specific output representation. */
internal interface ReportFormatter {
    fun format(findings: List<Finding>): String
}
