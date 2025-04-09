package com.woostockmanager

data class Order(
    val id: Int,
    val date: String,
    val status: String,
    val total: String,
    val currency: String
)
