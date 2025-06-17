package net.trueog.questsOG

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Bukkit

object MainThreadBlock {
    suspend fun <T> runOnMainThread(block: () -> T): T {
        return if (Bukkit.isPrimaryThread()) {
            block()
        } else {
            suspendCancellableCoroutine { cont ->
                Bukkit.getScheduler()
                    .runTask(
                        QuestsOG.plugin,
                        Runnable {
                            try {
                                cont.resume(block())
                            } catch (e: Throwable) {
                                cont.resumeWithException(e)
                            }
                        },
                    )
            }
        }
    }
}
