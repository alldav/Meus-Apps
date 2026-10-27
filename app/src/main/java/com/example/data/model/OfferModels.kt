package com.example.data.model

data class OfferItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // Eletrônicos, Perfumes, Casa, Moda, Utilidades, Saúde & Bem-Estar, Outros
    val marketplace: String, // Mercado Livre, Shopee, Amazon, Outro
    val couponCode: String?,
    val affiliateUrl: String,
    val priceTag: String,
    val discountBadge: String,
    val imageUrl: String = "",
    val postedAt: Long = System.currentTimeMillis()
)

data class ProductRequestItem(
    val id: String,
    val productName: String,
    val category: String,
    val userName: String,
    val createdAt: Long = System.currentTimeMillis()
)
