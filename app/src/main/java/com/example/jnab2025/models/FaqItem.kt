package com.example.jnab2025.models

data class FaqItem(
    val question: String,
    var answer: String = "",
    var isExpanded: Boolean = false,
    val isHeader: Boolean = false
)

