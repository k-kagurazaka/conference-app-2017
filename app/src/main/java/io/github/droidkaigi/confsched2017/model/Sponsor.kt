package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName

@Table
data class Sponsor @Setter constructor(
        @field:Column
        @field:SerializedName("image_url")
        var imageUrl: String,

        @field:Column
        @field:SerializedName("site_url")
        var url: String
)
