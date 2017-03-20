package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName

@Table
class MySession @Setter constructor(
        @field:PrimaryKey
        @field:Column(indexed = true)
        @field:SerializedName("id")
        var id: Int = 0,

        @field:Column(indexed = true, unique = true)
        @field:SerializedName("session")
        var session: Session
) {

    override fun equals(other: Any?): Boolean
            = other is MySession && other.id == id || super.equals(other)

    override fun hashCode(): Int = id
}
