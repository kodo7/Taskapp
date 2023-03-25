package com.example.taskapp

class Task (
    var taskName: String? = null,
    var taskDescription: String? = null,
    var points: Int? = null,
    var taskComplete: Boolean = false,
    var childId: String? = null
)