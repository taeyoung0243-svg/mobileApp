package com.example.soccerstore

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val imageUrl: String,
    val description: String
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)