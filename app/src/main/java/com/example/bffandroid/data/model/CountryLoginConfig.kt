package com.example.bffandroid.data.model

import com.google.gson.annotations.SerializedName

data class CountryLoginConfig(
    @SerializedName("countryIso2") val countryIso: String,
    @SerializedName("dialCode") val dialCode: Int?,
    @SerializedName("exampleNationalNumber") val exampleNationalNumber: String?,
    @SerializedName("loginMethod") val loginMethod: LoginMethod = LoginMethod.MobileNumber,
    @SerializedName("loginMethods") val loginMethods: List<CountryLoginMethod>? = null
)

data class CountryLoginMethod(
    @SerializedName("method") val method: String?
)
