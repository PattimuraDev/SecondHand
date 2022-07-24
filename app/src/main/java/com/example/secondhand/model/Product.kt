package com.example.secondhand.model

data class Product(
    val base_price: Int,
    val created_at: String,
    val description: String,
    val image_name: String,
    val image_url: String,
    val location: String,
    val name: String,
    val updated_at: String,
    val user_id: Int
)