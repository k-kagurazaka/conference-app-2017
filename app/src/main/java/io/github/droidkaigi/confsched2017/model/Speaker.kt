package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName
import io.github.droidkaigi.confsched2017.BuildConfig
import timber.log.Timber

@Table
class Speaker @Setter constructor(
        @field:PrimaryKey(auto = false)
        @field:Column(indexed = true)
        @field:SerializedName("id")
        var id: Int = 0,

        @field:Column(indexed = true)
        @field:SerializedName("name")
        var name: String,

        @field:Column
        @field:SerializedName("image_url")
        var imageUrl: String? = null,

        @field:Column
        @field:SerializedName("twitter_name")
        var twitterName: String? = null,

        @field:Column
        @field:SerializedName("github_name")
        var githubName: String? = null
) {

    val adjustedImageUrl: String?
        get() = imageUrl?.let {
            when {
                it.startsWith("http://") -> it
                it.startsWith("https://") -> it
                it.startsWith("/") -> BuildConfig.STATIC_ROOT + it
                else -> null.apply {
                    Timber.tag(TAG).e("Invalid image url: %s", it)
                }
            }
        }

    private companion object {

        val TAG: String = Speaker::class.java.simpleName
    }
}
