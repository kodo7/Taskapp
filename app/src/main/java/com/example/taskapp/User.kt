package com.example.taskapp

class User(fullName: String, email: String, parent: Boolean?, linkedAccounts: List<String>?) {
    val fullName = fullName
    val email = email
    val parent = parent
    val linkedAccounts = linkedAccounts
}