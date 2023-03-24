package com.example.taskapp

class User{
    var fullName:String? = null
    var email:String? = null
    var parent :Boolean? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User::class.java)
    }
    constructor(name: String?, email: String?, parent: Boolean?) {
        this.fullName = name
        this.email = email
        this.parent = parent
    }

}