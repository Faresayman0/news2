package com.example.whatnew

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsCallable {
    @GET("/v2/everything")
    fun getNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String
    ): Call<News>

    @GET("/v2/top-headlines")
    fun getNewsByCountry(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<News>

    // تعديل جديد لإضافة دالة لجلب الأخبار حسب الفئة
    @GET("/v2/top-headlines")
    fun getNewsByCategory(
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): Call<News>

    // تعديل جديد لإضافة دالة لجلب الأخبار حسب الدولة والفئة معاً
    @GET("/v2/top-headlines")
    fun getNewsByCountryAndCategory(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): Call<News>
}
