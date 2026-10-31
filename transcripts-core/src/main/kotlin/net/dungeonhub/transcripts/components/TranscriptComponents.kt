package net.dungeonhub.transcripts.components

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordServer

private fun loadResource(name: String): String =
    DiscordMessage::class.java.getResource("/$name")!!.readText()

private val stylesheet by lazy { loadResource("transcript.css") }
private val headResources by lazy { loadResource("transcript-head.html") }

/**
 * The full transcript document for [messages] of [channel]. Messages are rendered in the given order.
 */
fun HTML.transcript(channel: DiscordChannel<*>, messages: List<DiscordMessage>) {
    lang = "en"

    head {
        title {
            attributes["id"] = "transcriptTitle"
            +"#${channel.name} | ${messages.size} messages"
        }
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width")
        style { unsafe { +stylesheet } }
        // highlight.js, twemoji, helper scripts and svg icon symbols
        unsafe { +headResources }
    }

    body {
        preamble(channel.server, channel.name)
        div("chatlog") {
            id = "chatlog"
            messages.forEach { message(it) }
        }
    }
}

/**
 * The header showing the server icon, server name and channel name.
 */
fun FlowContent.preamble(server: DiscordServer, channelName: String) {
    div("preamble") {
        div("preamble__guild-icon-container") {
            img(alt = "Guild icon", src = server.icon.orEmpty(), classes = "preamble__guild-icon") { lazy() }
        }
        div("preamble__entries-container") {
            div("preamble__entry") {
                id = "guildname"
                +server.name
            }
            div("preamble__entry") {
                id = "ticketname"
                +"#$channelName"
            }
        }
    }
}
