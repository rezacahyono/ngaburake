package com.rezacah.ngaburake.report

/** Renders a list of [Finding]s into a specific output representation. */
internal interface ReportFormatter {
    fun format(findings: List<Finding>): String
}
