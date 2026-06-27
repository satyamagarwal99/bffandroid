package com.example.bffandroid.utils

object TokenUtils {
    fun getToken(): String {
        return if (AppSession.getString(Constant.ACCESS_TOKEN_KEY)==null || AppSession.getString(Constant.ACCESS_TOKEN_KEY)!!.isEmpty()) {
            ""
        } else {
            "Bearer ${AppSession.getString(Constant.ACCESS_TOKEN_KEY)!!}"
        }
    }
}
