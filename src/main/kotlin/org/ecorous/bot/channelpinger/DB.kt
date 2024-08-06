package org.ecorous.bot.channelpinger

import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import org.ecorous.bot.channelpinger.types.*
import org.ecorous.bot.channelpinger.types.ChannelOwnersTable.select
import org.ecorous.bot.channelpinger.types.ChannelPingsTable.select
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DB {
    lateinit var db: Database;
    fun init(): Boolean {
        if (this::db.isInitialized) {
            logger.warn("Tried to initialize the database twice - ignoring.")
            return false
        }
        try {
            val dbName = envOrNull("BOT_DATABASE") ?: "channelpinger";
            val user = envOrNull("BOT_DATABASE_USER") ?: "postgres";
            val password = envOrNull("BOT_DATABASE_PASSWORD") ?: "example";
            val dbHost = envOrNull("BOT_DATABASE_HOST") ?: "localhost";
            val dbPort = envOrNull("BOT_DATABASE_PORT") ?: "5432";
            val url = "jdbc:postgresql://$dbHost:$dbPort/$dbName";
            val driver = "org.postgresql.Driver";
            db = Database.connect(
                url,
                driver,
                user,
                password
            )

            transaction(db) { SchemaUtils.create(ChannelPingsTable, ChannelOwnersTable) }
            return true;
        } catch (e: Exception) {
            logger.error("Failed to initialize the database: $e");
            return false;
        }
    }

    fun getChannelOwner(id: Snowflake): Snowflake = getChannelOwner(id.value.toLong())

    fun getChannelOwner(id: Long): Snowflake = transaction(db) {
        return@transaction Snowflake(
            ChannelOwnersTable.selectAll().where(ChannelOwnersTable.channelId eq id).single()[ChannelOwnersTable.userId]
        )
    }

    fun getChannelOwner(id: String): Snowflake = getChannelOwner(id.toLong())

    fun addPingToChannel(userId: Snowflake, channelId: Snowflake) {
        transaction(db) {
            val res = ChannelPingsTable.selectAll().where { (ChannelPingsTable.userId eq userId.value.toLong()) and (ChannelPingsTable.channelId eq channelId.value.toLong()) }.any()
            if (res) return@transaction // Already exists
            ChannelPingsTable.insert {
                it[ChannelPingsTable.channelId] = channelId.value.toLong()
                it[ChannelPingsTable.userId] = userId.value.toLong()
            }
        }
    }

    fun removePingFromChannel(userId: Snowflake, channelId: Snowflake) {
        transaction(db) {
            ChannelPingsTable.deleteWhere {
                (ChannelPingsTable.userId eq userId.value.toLong()) and (ChannelPingsTable.channelId eq channelId.value.toLong())
            }
        }
    }

    fun setChannelOwner(channelId: Snowflake, userId: Snowflake) {
        transaction(db) {
            // remove any existing owners
            ChannelOwnersTable.deleteWhere { ChannelOwnersTable.channelId eq channelId.value.toLong() }
            ChannelOwnersTable.insert {
                it[ChannelOwnersTable.channelId] = channelId.value.toLong()
                it[ChannelOwnersTable.userId] = userId.value.toLong()
            }
        }
    }

    fun getChannelPings(channelId: Snowflake): List<Snowflake> = transaction(db) {
        return@transaction ChannelPingsTable.selectAll().where(ChannelPingsTable.channelId eq channelId.value.toLong()).map {
            Snowflake(it[ChannelPingsTable.userId])
        }
    }
}
