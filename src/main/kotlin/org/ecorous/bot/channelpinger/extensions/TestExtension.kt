package org.ecorous.bot.channelpinger.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.coalescingDefaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingString
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralUserCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Snowflake
import org.ecorous.bot.channelpinger.DB
import org.ecorous.bot.channelpinger.TEST_SERVER_ID

class TestExtension : Extension() {
	override val name = "test"

	override suspend fun setup() {
		publicSlashCommand {
			name = "pings"
			description = "Manage pings for this channel"
			publicSubCommand {
				name = "ping"
				description = "Ping the channel"
				action {
					val owner: Snowflake
					try {
						owner = DB.getChannelOwner(channel.id)
					} catch (_: NoSuchElementException) {
						respond { content = "No owner set for this channel" }
						return@action
					}
					if (user.id != owner && user.id != Snowflake(604653220341743618)) {
						respond {
							content = "You are not the owner of this channel."
						}
						return@action
					}
					val pings = DB.getChannelPings(channel.id)
					if (pings.isEmpty()) {
						respond { content = "No pings set for this channel" }
						return@action
					}
					val mentions = pings.joinToString(" ") { "<@${it}>" }
					respond {
						content = mentions
					}
				}
			}
			ephemeralSubCommand(::UserCommandArguments) {
				name = "add"
				description = "Add a ping to the channel"
				action {
					val owner: Snowflake
					try {
						owner = DB.getChannelOwner(channel.id)
					} catch (_: NoSuchElementException) {
						respond { content = "No owner set for this channel" }
						return@action
					}
					if (user.id != owner && user.id != Snowflake(604653220341743618)) {
						respond {
							content = "You are not the owner of this channel."
						}
						return@action
					}
					DB.addPingToChannel(arguments.user.id, channel.id)
					respond { content = "Added ping to channel" }
				}
			}
			ephemeralSubCommand(::UserCommandArguments) {
				name = "remove"
				description = "Remove a ping from the channel"
				action {
					val owner: Snowflake
					try {
						owner = DB.getChannelOwner(channel.id)
					} catch (_: NoSuchElementException) {
						respond { content = "No owner set for this channel" }
						return@action
					}
					if (user.id != owner && user.id != Snowflake(604653220341743618)) {
						respond {
							content = "You are not the owner of this channel."
						}
						return@action
					}
					DB.removePingFromChannel(arguments.user.id, channel.id)
					respond { content = "Removed ping from channel" }
				}
			}
			ephemeralSubCommand {
				name = "view"
				description = "View the pings for this channel"
				action {
					val owner: Snowflake
					try {
						owner = DB.getChannelOwner(channel.id)
					} catch (_: NoSuchElementException) {
						respond { content = "No owner set for this channel" }
						return@action
					}
					if (user.id != owner && user.id != Snowflake(604653220341743618)) {
						respond {
							content = "You are not the owner of this channel."
						}
						return@action
					}
					val pings = DB.getChannelPings(channel.id)
					if (pings.isEmpty()) {
						respond { content = "No pings set for this channel" }
						return@action
					}
					val mentions = pings.joinToString(" ") { "<@${it}>" }
					respond {
						content = mentions
					}
				}
			}
		}
		ephemeralUserCommand {
			name = "Set channel owner"
			action {
				if (user.id != Snowflake(604653220341743618)) {
					respond {
						content = "You do not have permission to set the channel owner."
					}
					return@action
				}

				val owner = targetUsers.first().id
				DB.setChannelOwner(channel.id, owner)
				respond {
					content = "Set channel owner to <@$owner>"
				}
			}
		}
		ephemeralUserCommand {
			name = "Add user to channel"

			action {
				val owner: Snowflake
				try {
					owner = DB.getChannelOwner(channel.id)
				} catch (_: NoSuchElementException) {
					respond { content = "No owner set for this channel" }
					return@action
				}
				if (user.id != owner && user.id != Snowflake(604653220341743618)) {
					respond {
						content = "You are not the owner of this channel."
					}
					return@action
				}
				DB.addPingToChannel(targetUsers.first().id, channel.id)
				respond {
					content = "Added user to channel"
				}
			}
		}
		ephemeralUserCommand {
			name = "Remove user from channel"

			action {
				val owner: Snowflake
				try {
					owner = DB.getChannelOwner(channel.id)
				} catch (_: NoSuchElementException) {
					respond { content = "No owner set for this channel" }
					return@action
				}
				if (user.id != owner && user.id != Snowflake(604653220341743618)) {
					respond {
						content = "You are not the owner of this channel."
					}
					return@action
				}
				DB.removePingFromChannel(targetUsers.first().id, channel.id)
				respond {
					content = "Removed user from channel"
				}
			}
		}
	}
	inner class UserCommandArguments : Arguments() {
		val user by user {
			name = "user"
			description = "The user to add/remove to the channel"
		}
	}

}
