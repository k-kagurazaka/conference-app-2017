package io.github.droidkaigi.confsched2017.api.service

import io.github.droidkaigi.confsched2017.model.Contributor
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {

    @GET("/repos/{owner}/{repo}/contributors")
    fun getContributors(@Path("owner") owner: String, @Path("repo") repo: String,
                        @Query("anon") anon: Int, @Query("per_page") perPage: Int): Call<List<Contributor>>

}
