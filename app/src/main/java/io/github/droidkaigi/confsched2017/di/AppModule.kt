package io.github.droidkaigi.confsched2017.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2017.BuildConfig
import io.github.droidkaigi.confsched2017.api.RequestInterceptor
import io.github.droidkaigi.confsched2017.api.service.DroidKaigiService
import io.github.droidkaigi.confsched2017.api.service.GithubService
import io.github.droidkaigi.confsched2017.api.service.GoogleFormService
import io.github.droidkaigi.confsched2017.repository.OrmaHolder
import io.github.droidkaigi.confsched2017.repository.OrmaHolderImpl
import io.github.droidkaigi.confsched2017.util.ThreadDispatcher
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import kotlin.coroutines.experimental.CoroutineContext

@Module
class AppModule(private val context: Context) {

    @Provides
    fun provideContext(): Context = context

    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences
            = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideThreadDispatcher(): ThreadDispatcher = object : ThreadDispatcher() {
        override val UI: CoroutineContext
            get() = kotlinx.coroutines.experimental.android.UI

        override val worker: CoroutineContext
            get() = CommonPool
    }

    @Provides
    fun provideCancellationJob(): Job = Job()

    @Provides
    fun provideCompositeDisposable(): CompositeDisposable = CompositeDisposable()

    @Provides
    fun provideRequestInterceptor(interceptor: RequestInterceptor): Interceptor = interceptor

    @Provides
    fun provideOrmaHolder(ormaHolder: OrmaHolderImpl): OrmaHolder = ormaHolder

    @Singleton
    @Provides
    fun provideDroidKaigiService(client: OkHttpClient): DroidKaigiService =
            Retrofit.Builder()
                    .client(client)
                    .baseUrl(BuildConfig.API_ROOT)
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .build()
                    .create(DroidKaigiService::class.java)

    @Singleton
    @Provides
    fun provideGithubService(client: OkHttpClient): GithubService =
            Retrofit.Builder().client(client)
                    .baseUrl("https://api.github.com")
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .build()
                    .create(GithubService::class.java)

    @Singleton
    @Provides
    fun provideGoogleFormService(client: OkHttpClient): GoogleFormService =
            Retrofit.Builder().client(client)
                    .baseUrl("https://docs.google.com/forms/d/")
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .build()
                    .create(GoogleFormService::class.java)

    private companion object {

        val SHARED_PREF_NAME = "preferences"

        fun createGson(): Gson = GsonBuilder().setDateFormat("yyyy/MM/dd HH:mm:ss").create()
    }
}
