package net.trueog.questsOG

open class Requirement(val name: String)

class ProgressRequirement(name: String, val current: Int, val target: Int) : Requirement(name)

class BooleanRequirement(name: String, val met: Boolean) : Requirement(name)