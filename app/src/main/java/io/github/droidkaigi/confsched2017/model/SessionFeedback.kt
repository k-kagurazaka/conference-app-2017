package io.github.droidkaigi.confsched2017.model

import com.github.gfx.android.orma.annotation.Column
import com.github.gfx.android.orma.annotation.PrimaryKey
import com.github.gfx.android.orma.annotation.Setter
import com.github.gfx.android.orma.annotation.Table
import com.google.gson.annotations.SerializedName

@Table
class SessionFeedback @Setter constructor(
        @field:PrimaryKey(auto = false)
        @field:Column(indexed = true)
        @field:SerializedName("session_id")
        var sessionId: Int,

        @field:Column
        @field:SerializedName("session_title")
        var sessionTitle: String,

        @field:Column
        @field:SerializedName("relevancy")
        var relevancy: Int,

        @field:Column
        @field:SerializedName("as_expected")
        var asExpected: Int,

        @field:Column
        @field:SerializedName("difficulty")
        var difficulty: Int,

        @field:Column
        @field:SerializedName("knowledgeable")
        var knowledgeable: Int,

        @field:Column
        @field:SerializedName("comment")
        var comment: String? = null,

        @field:Column
        @field:SerializedName("is_submitted")
        var isSubmitted: Boolean = false
) {

    companion object {
        @JvmStatic // TODO remove later
        fun create(session: Session, relevancy: Int, asExpected: Int, difficulty: Int, knowledgeable: Int, comment: String?) =
                SessionFeedback(
                        sessionId = session.id,
                        sessionTitle = session.title,
                        relevancy = relevancy,
                        asExpected = asExpected,
                        difficulty = difficulty,
                        knowledgeable = knowledgeable,
                        comment = comment
                )
    }

    val isAllFilled: Boolean
        get() = sessionId > 0
                && relevancy > 0
                && asExpected > 0
                && difficulty > 0
                && knowledgeable > 0
}
