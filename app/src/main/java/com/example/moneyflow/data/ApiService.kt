package com.example.moneyflow.data

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface ApiService {

    @GET("latest.js")
    fun getCurrencyRates(): Single<CurrencyResponse>
}
