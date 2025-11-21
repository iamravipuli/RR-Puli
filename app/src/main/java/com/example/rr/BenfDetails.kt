package com.example.rrpuli

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BenfDetails(
    val id: String = "",
    val name: String = "",
    val amount: Long = 0,
    val date: String = "",
    val iRate: String = "0.00",
    val remarks: String = ""
) : Parcelable
