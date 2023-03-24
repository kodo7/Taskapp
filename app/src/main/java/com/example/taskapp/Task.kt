package com.example.taskapp

class Task (val taskName: String?, val taskDescription: String?, val points: Int?, val taskComplete: Boolean) {
    @JvmName("getTaskDescription1")
    fun getTaskDescription(): String? {
        return taskDescription
    }

    fun isTaskComplete(): Boolean {
        return taskComplete
    }

    @JvmName("getPoints1")
    fun getPoints(): Int? {
        return points
    }

    @JvmName("getTaskName1")
    fun getTaskName(): String? {
        return taskName
    }
}