package com.example.taskapp

data class Child(
    var childId: String? = null,
    var userId: String? = null,
    var email: String? = null,
    var currentPoints: Int = 0,
    val name: String? = null
)