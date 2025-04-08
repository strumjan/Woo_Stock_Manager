package com.woostockmanager

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    var stockQuantity: Int, // Може да се менува
    var stockStatus: String // Променето во var!
)

