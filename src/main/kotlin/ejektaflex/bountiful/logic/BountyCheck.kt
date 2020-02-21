package ejektaflex.bountiful.logic

import ejektaflex.bountiful.api.data.IBountyData
import ejektaflex.bountiful.api.data.entry.BountyEntry
import ejektaflex.bountiful.api.data.entry.BountyEntryStack
import ejektaflex.bountiful.api.ext.modOriginName
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

class BountyCheck(val player: PlayerEntity, val data: IBountyData, val inv: NonNullList<ItemStack>) {



    val partMap = mutableMapOf<ItemStack, StackPartition>()

    fun checkStacks() {
        partMap.clear()

        val stackObjs = data.objectives.content.filterIsInstance<BountyEntryStack>()

        val stackTypeObj = stackObjs.filter { it.type == "stack" }
        val tagTypeObj = stackObjs.filter { it.type == "tag" }

        println("Objectives: $stackObjs")

        // For each stack objective
        println("Checking stacks")
        val a = checkObjs(stackTypeObj)
        println("Checking tags")
        val b = checkObjs(tagTypeObj)

        println("Fully reserved partitions: ${partMap.count { it.value.free == 0 }}")

        println("A: $a")
        println("B: $b")


    }

    private fun checkObjs(list: List<BountyEntryStack>): Map<BountyEntry, BountyProgress> {

        var succ = mutableMapOf<BountyEntry, BountyProgress>()

        // For each stack objective
        loop@ for (obj in list) {

            var neededForObj = obj.amount

            // Get all matching inventory stacks
            val invStacks = inv.filter { validStackCheck(obj.validStacks, it) }
            for (iStack in invStacks) {
                println("Analyzing stack: $iStack")
                // Initialize the stack in the partmap
                if (iStack !in partMap) {
                    partMap[iStack] = StackPartition(iStack)
                }
                // Grab it
                val part = partMap[iStack]!!

                println("PartMapSize: ${partMap.keys}")
                println("Partition reserving in: $part")

                println("Trying to reserve: $neededForObj")

                val leftOver = part.reserve(neededForObj)

                println("Leftover after reserving: $leftOver")

                // If we have nothing leftover (AKA allocated it all), we are done with this item stack
                if (leftOver == 0) {
                    neededForObj = 0
                    break
                } else {
                    neededForObj = leftOver
                }
            }

            when (neededForObj) {
                0 -> {
                    println("Got it all! / AKA SUCC 1 OBJ")
                    succ[obj] = BountyProgress.DONE
                }
                obj.amount -> {
                    succ[obj] = BountyProgress.NEW
                }
                else -> {
                    println("Needed this many more: $neededForObj / AKA FAILED 1 OBJ")
                    succ[obj] = BountyProgress.UNFINISHED
                    //break@loop
                }
            }


        }

        return succ

    }


    private fun validStackCheck(stacks: List<ItemStack>, other: ItemStack): Boolean {
        return stacks.any { stack -> stack.isItemEqualIgnoreDurability(other) && ItemStack.areItemStackTagsEqual(stack, other) }
    }


}