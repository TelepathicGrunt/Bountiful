package ejektaflex.bountiful.data.json

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import ejektaflex.bountiful.BountifulMod
import ejektaflex.bountiful.SetupLifecycle
import ejektaflex.bountiful.data.bounty.enums.BountifulResourceType
import ejektaflex.bountiful.data.bounty.enums.BountyType
import ejektaflex.bountiful.data.registry.DecreeRegistry
import ejektaflex.bountiful.data.registry.PoolRegistry
import ejektaflex.bountiful.data.structure.Decree
import ejektaflex.bountiful.data.structure.EntryPool
import ejektaflex.bountiful.util.ValueRegistry
import net.minecraft.client.resources.JsonReloadListener
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.item.crafting.RecipeManager
import net.minecraft.profiler.IProfiler
import net.minecraft.resources.IResourceManager
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.fml.ModList
import java.lang.IllegalArgumentException
import java.util.function.Function

class BountyReloadListener : JsonReloadListener(JsonAdapter.gson, "bounties") {

    override fun apply(
        objectIn: MutableMap<ResourceLocation, JsonElement>,
        resourceManagerIn: IResourceManager,
        profilerIn: IProfiler
    ) {

        val decreeMap = mutableMapOf<String, MutableList<Decree>>()
        val poolMap = mutableMapOf<String, MutableList<EntryPool>>()

        for ((rl, element) in objectIn) {
            if (rl.path.startsWith("_")) { continue }

            println("BoReloader wants to load resource at: $rl. It is: $element")
            val toGrab = rl.path.substringBefore('/')

            println("TOGRAB: $toGrab")

            val typeOfFile = BountifulResourceType.values().find { it.folderName == toGrab } ?: continue // ignore non resource types!
            println("BoReloader has type of file: $typeOfFile")

            val loaded = JsonAdapter.fromJson(element, typeOfFile.klazz)

            println("BoReloader loaded this: $loaded")

            when (loaded) {
                is Decree -> decreeMap.getOrPut(rl.path.substringAfter('/')) { mutableListOf() }.add(loaded.also { it.id =
                    BountifulMod.rlFileNameNoExt(rl)
                })
                is EntryPool -> poolMap.getOrPut(rl.path.substringAfter('/').substringAfter('/').also { println("DERP: $it") }) { mutableListOf() }.add(loaded.also { it.id =
                    BountifulMod.rlFileNameNoExt(rl)
                })
            }
        }

        // Merge decrees and pools based on paths

        val decreesMapped = decreeMap.map { entry -> entry.key to entry.value.reduce { a, b ->
            if (b.canLoad) a.merge(b) else a
        } }.toMap().values.toList()

        DecreeRegistry.restore(decreesMapped)

        BountifulMod.logger.info("Found decrees: ${DecreeRegistry.ids}")

        val poolsMapped = poolMap.map { entry -> entry.key to entry.value.reduce { a, b ->
            if (b.canLoad) a.merge(b) else a
        } }.toMap().values.toList()


        BountifulMod.logger.info("Validating Entry Pools..")

        poolsMapped.forEach {
            BountifulMod.logger.info("Validating pool '${it.id}'")
            val invalid = SetupLifecycle.validatePool(it, null, false)
            for (item in invalid) {
                BountifulMod.logger.warn("Invalid pool: $item")
            }
        }

        PoolRegistry.restore(poolsMapped)

        BountifulMod.logger.info("Found Entry Pools: ${PoolRegistry.ids}")

    }

    fun <T : Any> restore(reg: ValueRegistry<T>, inMap: Map<String, List<T>>) {

        ModList.get().mods.map { it.modId }

    }


}