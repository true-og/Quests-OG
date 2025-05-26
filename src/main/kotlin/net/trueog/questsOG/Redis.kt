package net.trueog.questsOG

import io.lettuce.core.RedisClient

class Redis {
    private val redisClient: RedisClient = RedisClient.create(QuestsOG.config.redisUrl)

    fun getValue(key: String): String? {
        val connection = redisClient.connect()
        val commands = connection.sync()
        val value = commands.get(key)
        connection.close()
        return value
    }

    fun setValue(key: String, value: String) {
        val connection = redisClient.connect()
        val commands = connection.sync()
        commands.set(key, value)
        connection.close()
        return
    }

    fun shutdown() {
        redisClient.shutdown()
    }
}