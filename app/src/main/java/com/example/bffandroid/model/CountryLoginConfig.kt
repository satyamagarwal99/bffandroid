package com.example.bffandroid.model

data class CountryLoginConfig(
    val countryIso: String,
    val dialCode: Int?,
    val exampleNationalNumber: String?,
    val loginMethod: LoginMethod
)
