package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName

@Table
class Topic @Setter constructor(
        @field:PrimaryKey(auto = false)
        @field:Column(indexed = true)
        @field:SerializedName("id")
        var id: Int = 0,

        @field:Column(indexed = true)
        @field:SerializedName("name")
        var name: String,

        @field:Column
        @field:SerializedName("other")
        var other: String? = null
)
