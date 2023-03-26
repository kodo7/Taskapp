package com.example.taskapp

data class Task (
    var taskId: String = "null",
    var taskName: String? = null,
    var taskDescription: String? = null,
    var points: Int? = null,
    var taskComplete: Boolean = false,
    var childId: String? = null,
    var verified: Boolean = false
)