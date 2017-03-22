package io.github.droidkaigi.confsched2017.api.service

import io.github.droidkaigi.confsched2017.model.Session
import retrofit2.Call
import retrofit2.http.GET

interface DroidKaigiService {

    @GET("sessions.json")
    fun getSessionsJa(): Call<List<Session>>

    @GET("en/sessions.json")
    fun getSessionsEn(): Call<List<Session>>

}
