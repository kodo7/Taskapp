package com.example.taskapp

import java.time.LocalDate

data class Deposit(
    val depositId: String,
    val childId: String,
    val amount: Int,
    val interestRate: Int,
    val startDate: LocalDate,
    val status: String
)