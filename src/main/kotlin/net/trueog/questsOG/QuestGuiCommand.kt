package net.trueog.questsOG

import kotlinx.coroutines.launch
import net.trueog.gxui.advancements.AdvancementMenu
import net.trueog.gxui.advancements.AdvancementState
import net.trueog.questsOG.progression.HomesProgression
import net.trueog.questsOG.quests.Quest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QuestGuiCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val debug = QuestsOG.config.debug
        if (debug) QuestsOG.plugin.logger.info("/questgui invoked by ${sender.name}")
        if (sender !is Player) {
            sender.sendMessage("ERROR: You can only execute this command as a player.")
            return true
        }

        QuestsOG.scope.launch {
            try {
                if (debug) QuestsOG.plugin.logger.info("/questgui coroutine entered for ${sender.name}")
                val nextQuest = HomesProgression.getNextQuest(sender)
                if (debug) QuestsOG.plugin.logger.info("/questgui nextQuest=${nextQuest?.javaClass?.simpleName}")
                val builder = AdvancementMenu.builder(QuestsOG.plugin, sender, "&aHome Quests")

                HomesProgression.quests.forEach { quest ->
                    builder.add(questName(quest), stateFor(quest, nextQuest))
                    quest.getRequirements(sender)?.forEach { requirement ->
                        when (requirement) {
                            is ProgressRequirement ->
                                builder.progress(requirement.name, requirement.current, requirement.target)
                            is BooleanRequirement -> builder.bool(requirement.name, requirement.met)
                        }
                    }
                    if (quest == nextQuest) builder.footer("&7Use &e/claimquest &7when ready.")
                }

                if (debug) QuestsOG.plugin.logger.info("/questgui dispatching builder.open on main thread")
                MainThreadBlock.runOnMainThread { builder.open(args) }
                if (debug) QuestsOG.plugin.logger.info("/questgui done")
            } catch (t: Throwable) {
                QuestsOG.plugin.logger.severe("/questgui failed: ${t.message}")
                if (debug) t.printStackTrace()
            }
        }

        return true
    }

    private fun questName(quest: Quest): String = "Home ${HomesProgression.getHomeCount(quest)} Quest"

    private fun stateFor(quest: Quest, nextQuest: Quest?): AdvancementState =
        when {
            nextQuest == null -> AdvancementState.COMPLETE
            quest == nextQuest -> AdvancementState.IN_PROGRESS
            HomesProgression.quests.indexOf(quest) < HomesProgression.quests.indexOf(nextQuest) ->
                AdvancementState.COMPLETE
            else -> AdvancementState.LOCKED
        }
}
