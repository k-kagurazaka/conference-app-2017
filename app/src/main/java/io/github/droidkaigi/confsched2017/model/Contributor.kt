package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.*
import com.google.gson.annotations.SerializedName

@Table
class Contributor(
        @field:PrimaryKey(auto = false)
        @field:Column
        @param:Setter
        @field:SerializedName(value = "login", alternate = arrayOf("name"))
        var name: String,

        @field:Column("avatar_url")
        @get:Getter("avatar_url")
        @param:Setter("avatar_url")
        @field:SerializedName("avatar_url")
        var avatarUrl: String? = null,

        @field:Column("html_url")
        @get:Getter("html_url")
        @param:Setter("html_url")
        @field:SerializedName("html_url")
        var htmlUrl: String? = null,

        @field:Column
        @param:Setter
        @field:SerializedName("contributions")
        var contributions: Int
) {
    override fun equals(other: Any?): Boolean
            = other is Contributor && other.name == name || super.equals(other)

    override fun hashCode(): Int = name.hashCode()
}
