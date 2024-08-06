package org.ecorous.bot.channelpinger.types

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object ChannelOwnersTable : Table() {
    val channelId = long("channelId")
    val userId = long("userId")

    override val primaryKey = PrimaryKey(channelId)
}

object ChannelPingsTable : Table() {
    val channelId = long("channelId")
    val userId = long("userId")

}