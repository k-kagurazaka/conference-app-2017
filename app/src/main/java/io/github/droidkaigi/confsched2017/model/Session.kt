package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName
import java.util.*

@Table
class Session @Setter constructor(
        @field:PrimaryKey(auto = false)
        @field:Column(indexed = true)
        @field:SerializedName("id")
        var id: Int,

        @field:Column(indexed = true)
        @field:SerializedName("title")
        var title: String,

        @field:Column
        @field:SerializedName("desc")
        var desc: String? = null,

        @field:Column(indexed = true)
        @field:SerializedName("speaker")
        var speaker: Speaker? = null,

        @field:Column
        @field:SerializedName("stime")
        var stime: Date,

        @field:Column
        @field:SerializedName("etime")
        var etime: Date,

        @field:Column
        @field:SerializedName("duration_min")
        var durationMin: Int,

        @field:Column
        @field:SerializedName("type")
        var type: String,

        @field:Column(indexed = true)
        @field:SerializedName("topic")
        var topic: Topic? = null,

        @field:Column(indexed = true)
        @field:SerializedName("room")
        var room: Room? = null,

        @field:Column
        @field:SerializedName("lang")
        var lang: String? = null,

        @field:Column
        @field:SerializedName("slide_url")
        var slideUrl: String? = null,

        @field:Column
        @field:SerializedName("movie_url")
        var movieUrl: String? = null,

        @field:Column
        @field:SerializedName("movie_dash_url")
        var movieDashUrl: String? = null,

        @field:Column
        @field:SerializedName("share_url")
        var shareUrl: String? = null
) {

    private enum class Type {
        CEREMONY, SESSION, BREAK, DINNER;

        fun matches(type: String): Boolean = name.equals(type, ignoreCase = true)
    }

    val isSession: Boolean
        get() = Type.SESSION.matches(type)

    val isCeremony: Boolean
        get() = Type.CEREMONY.matches(type)

    val isBreak: Boolean
        get() = Type.BREAK.matches(type)

    val isDinner: Boolean
        get() = Type.DINNER.matches(type)

    fun isLiveAt(whenever: Date): Boolean
            = stime.before(whenever) && etime.after(whenever)

    override fun equals(other: Any?): Boolean
            = other is Session && other.id == id || super.equals(other)

    override fun hashCode(): Int = id
}
