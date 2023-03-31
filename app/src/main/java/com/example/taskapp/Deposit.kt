package com.example.taskapp

import java.time.LocalDate

data class Deposit(
    val depositId: String = "",
    val childId: String = "",
    val amount: Int = 0,
    val interestRate: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    var status: String = ""
)