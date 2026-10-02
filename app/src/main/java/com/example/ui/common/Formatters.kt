package com.example.ui.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val indonesianLocale = Locale("id", "ID")

    private val rupiahSymbols = DecimalFormatSymbols(indonesianLocale).apply {
        currencySymbol = "Rp "
        groupingSeparator = '.'
        monetaryDecimalSeparator = ','
    }

    private val currencyFormat = DecimalFormat("Rp #,##0", rupiahSymbols)
    private val numberFormat = DecimalFormat("#,##0", rupiahSymbols)

    fun formatRupiah(amount: Double): String {
        return currencyFormat.format(amount)
    }

    fun formatNumber(number: Number): String {
        return numberFormat.format(number)
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", indonesianLocale)
        return sdf.format(Date(timestamp))
    }
}
