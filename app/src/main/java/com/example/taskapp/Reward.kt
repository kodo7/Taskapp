package com.example.taskapp

data class Reward(
    var rewardId: String? = null,
    var childId: String? = null,
    var rewardName: String? = null,
    var cost: Int = 0,
    val qty: Int = 0,
    val boughtQty: Int = 0
)