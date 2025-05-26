package net.trueog.questsOG

import io.lettuce.core.RedisClient

class Redis {
    private val redisClient: RedisClient = RedisClient.create(QuestsOG.config.redisUrl)

    fun getValue(key: String): String? {
        val connection = redisClient.connect()
        val commands = connection.sync()
        return commands.get(key)
    }

    fun setValue(key: String, value: String): String? {
        val connection = redisClient.connect()
        val commands = connection.sync()
        return commands.set(key, value)
    }
}