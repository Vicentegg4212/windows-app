package mx.cires.sasmex.android.data

import java.util.Date

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val time: Long = System.currentTimeMillis()
) {
    val date: Date get() = Date(time)
}
