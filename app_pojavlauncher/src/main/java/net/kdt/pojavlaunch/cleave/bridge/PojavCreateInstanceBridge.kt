/*
 * CLEAVE Launcher UI layer for PojavLauncher.
 * Adapter behind the CLEAVE "Create New Instance" page. Every install path calls the same Pojav backend
 * code the Pojav installer fragments call (FabriclikeInstallFragment, ForgelikeInstallFragment,
 * OptiFineInstallFragment, BTAInstallFragment, ProfileTypeSelectFragment vanilla button):
 *   Fabric / Quilt / Legacy Fabric  FabriclikeUtils.install + Instances.createInstance
 *   Forge / NeoForge                ForgelikeUtils.createInstaller + Instances.createInstance + InstanceInstaller.start
 *   OptiFine                        OptiFineDownloadTask.prepareForInstall + OptiFineUtils.createInstaller
 *   BTA                             BTADownloadTask
 *   Vanilla                         Instances.createInstance with the picked version id
 * Replaces the install flow of mirai-launcher-
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/ui/screens/content/download/game/DownloadGameWithAddonScreen.kt
 *   MiraiLauncher/src/main/java/com/movtery/zalithlauncher/game/download/game/GameInstaller.kt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package net.kdt.pojavlaunch.cleave.bridge

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import net.kdt.pojavlaunch.JVersionList
import net.kdt.pojavlaunch.PojavApplication
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.extra.ExtraConstants
import net.kdt.pojavlaunch.extra.ExtraCore
import net.kdt.pojavlaunch.extra.ExtraListener
import net.kdt.pojavlaunch.game.renderer.RendererDefaults
import net.kdt.pojavlaunch.instances.Instance
import net.kdt.pojavlaunch.instances.Instances
import net.kdt.pojavlaunch.modloaders.BTADownloadTask
import net.kdt.pojavlaunch.modloaders.BTAUtils
import net.kdt.pojavlaunch.modloaders.FabriclikeUtils
import net.kdt.pojavlaunch.modloaders.ForgelikeUtils
import net.kdt.pojavlaunch.modloaders.ModloaderDownloadListener
import net.kdt.pojavlaunch.modloaders.OptiFineDownloadTask
import net.kdt.pojavlaunch.modloaders.OptiFineUtils
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import net.kdt.pojavlaunch.instances.InstanceIconProvider
import com.kdt.mcgui.ProgressLayout
import java.io.File
import java.io.IOException

/** Loader tiles of the Create New Instance page, in the order of the CLEAVE design. */
enum class CleaveLoader(
    val displayName: String,
    @DrawableRes val iconRes: Int,
    @StringRes val taglineRes: Int,
    /** Whether the loader needs a Minecraft version from the list on the left. */
    val usesGameVersion: Boolean,
    /**
     * Whether the loader publishes builds to choose from. Vanilla has none - there is nothing to
     * pick between - so a vanilla instance is complete as soon as a Minecraft version is selected.
     */
    val hasBuilds: Boolean
) {
    VANILLA("Vanilla", R.drawable.cleave_loader_vanilla, R.string.cleave_loader_vanilla_sub, true, false),
    OPTIFINE("OptiFine", R.drawable.cleave_loader_optifine, R.string.cleave_loader_optifine_sub, true, true),
    FABRIC("Fabric", R.drawable.ic_fabric, R.string.cleave_loader_fabric_sub, true, true),
    FORGE("Forge", R.drawable.ic_forge, R.string.cleave_loader_forge_sub, true, true),
    NEOFORGE("NeoForge", R.drawable.ic_neoforge, R.string.cleave_loader_neoforge_sub, true, true),
    QUILT("Quilt", R.drawable.ic_quilt, R.string.cleave_loader_quilt_sub, true, true),
    LEGACY_FABRIC("Legacy Fabric", R.drawable.cleave_loader_legacy_fabric, R.string.cleave_loader_legacy_fabric_sub, true, true),
    BTA("BTA", R.drawable.cleave_loader_bta, R.string.cleave_loader_bta_sub, false, true)
}

/** Version groups of Pojav's VersionListAdapter (installed, release, snapshot, old_beta, old_alpha). */
enum class CleaveVersionType { RELEASE, SNAPSHOT, OLD_BETA, OLD_ALPHA, INSTALLED }

class CleaveGameVersion(val id: String, val type: CleaveVersionType)

/** One selectable loader build. [source] is the untouched Pojav object (OptiFineVersion, BTAVersion, version string). */
class CleaveLoaderVersion(val id: String, val label: String, val stable: Boolean, val source: Any?)

object PojavCreateInstanceBridge {
    /** Bumped when Pojav publishes the Mojang version manifest (ExtraConstants.RELEASE_TABLE). */
    var versionTableRevision by mutableIntStateOf(0)
        private set
    var installing by mutableStateOf(false)
        private set

    private var attached = false
    private var optiFineCache: OptiFineUtils.OptiFineVersions? = null
    private var btaCache: BTAUtils.BTAVersionList? = null

    // Held strongly: ExtraCore keeps only weak references.
    private val releaseTableListener = ExtraListener<Any> { _, _ ->
        Tools.runOnUiThread { versionTableRevision++ }
        false
    }

    fun attach() {
        if (attached) return
        attached = true
        ExtraCore.addExtraListener(ExtraConstants.RELEASE_TABLE, releaseTableListener)
    }

    /** Same sources as Pojav's VersionListAdapter: the manifest in ExtraConstants.RELEASE_TABLE and .minecraft/versions. */
    fun gameVersions(): List<CleaveGameVersion> {
        val result = ArrayList<CleaveGameVersion>()
        val installed = File(Tools.DIR_GAME_NEW, "versions").list()
        if (installed != null) {
            installed.sorted().forEach { result.add(CleaveGameVersion(it, CleaveVersionType.INSTALLED)) }
        }
        val table = ExtraCore.getValue(ExtraConstants.RELEASE_TABLE) as? JVersionList
        table?.versions?.forEach { version ->
            val id = version.id ?: return@forEach
            val type = when (version.type) {
                "release" -> CleaveVersionType.RELEASE
                "snapshot" -> CleaveVersionType.SNAPSHOT
                "old_beta" -> CleaveVersionType.OLD_BETA
                "old_alpha" -> CleaveVersionType.OLD_ALPHA
                else -> return@forEach
            }
            result.add(CleaveGameVersion(id, type))
        }
        return result
    }

    /** Builds available for [loader] on [gameVersion], newest first. Empty when the loader has none for that version. */
    @WorkerThread
    @Throws(IOException::class)
    fun loadLoaderVersions(loader: CleaveLoader, gameVersion: String?): List<CleaveLoaderVersion> {
        return when (loader) {
            CleaveLoader.VANILLA -> emptyList()
            CleaveLoader.FABRIC -> fabriclike(FabriclikeUtils.FABRIC_UTILS, gameVersion)
            CleaveLoader.QUILT -> fabriclike(FabriclikeUtils.QUILT_UTILS, gameVersion)
            CleaveLoader.LEGACY_FABRIC -> fabriclike(FabriclikeUtils.LEGACY_FABRIC_UTILS, gameVersion)
            CleaveLoader.FORGE -> forgelike(ForgelikeUtils.FORGE_UTILS, gameVersion)
            CleaveLoader.NEOFORGE -> forgelike(ForgelikeUtils.NEOFORGE_UTILS, gameVersion)
            CleaveLoader.OPTIFINE -> optifine(gameVersion)
            CleaveLoader.BTA -> bta()
        }
    }

    private fun fabriclike(utils: FabriclikeUtils, gameVersion: String?): List<CleaveLoaderVersion> {
        if (gameVersion == null) return emptyList()
        // The loader endpoint answers for any id; only offer it when the game version is supported.
        val games = utils.downloadGameVersions() ?: return emptyList()
        if (games.none { it.version == gameVersion }) return emptyList()
        val loaders = utils.downloadLoaderVersions(gameVersion) ?: return emptyList()
        return loaders.map { CleaveLoaderVersion(it.version, it.version, it.stable, it.version) }
    }

    private fun forgelike(utils: ForgelikeUtils, gameVersion: String?): List<CleaveLoaderVersion> {
        if (gameVersion == null) return emptyList()
        val all = utils.downloadVersions() ?: return emptyList()
        // Grouping and ordering identical to ForgelikeVersionListAdapter.
        val matching = all.filter { !utils.shouldSkipVersion(it) && utils.processVersionString(it) == gameVersion }
        val ordered = if (utils.isVersionOrderInversed) matching.reversed() else matching
        return ordered.map { full -> CleaveLoaderVersion(full, full, true, full) }
    }

    private fun optifine(gameVersion: String?): List<CleaveLoaderVersion> {
        if (gameVersion == null) return emptyList()
        val versions = optiFineCache ?: OptiFineUtils.downloadOptiFineVersions()?.also { optiFineCache = it } ?: return emptyList()
        val index = versions.gameVersions.indexOfFirst { it.removePrefix("Minecraft").trim() == gameVersion }
        if (index < 0) return emptyList()
        return versions.optifineVersions[index].map { version ->
            val label = version.versionName.removePrefix("OptiFine").trim()
            CleaveLoaderVersion(version.versionName, label, !version.versionName.contains("pre", ignoreCase = true), version)
        }
    }

    private fun bta(): List<CleaveLoaderVersion> {
        val list = btaCache ?: BTAUtils.downloadVersionList()?.also { btaCache = it } ?: return emptyList()
        val result = ArrayList<CleaveLoaderVersion>()
        list.testedVersions.forEach { result.add(CleaveLoaderVersion(it.versionName, it.versionName, true, it)) }
        list.untestedVersions.forEach { result.add(CleaveLoaderVersion(it.versionName, it.versionName, false, it)) }
        list.nightlyVersions.forEach { result.add(CleaveLoaderVersion(it.versionName, it.versionName, false, it)) }
        return result
    }

    /** Name proposed in the Instance Info field, e.g. "Fabric 1.21.1". */
    fun suggestedName(loader: CleaveLoader, gameVersion: String?, loaderVersion: CleaveLoaderVersion?): String = when (loader) {
        CleaveLoader.BTA -> "BTA " + (loaderVersion?.label ?: "")
        CleaveLoader.VANILLA -> gameVersion ?: ""
        else -> loader.displayName + " " + (gameVersion ?: "")
    }.trim()

    /**
     * Creates the instance. Runs on Pojav's executor; [onFinished] is posted to the UI thread with true on success.
     * The created instance becomes the selected one and receives [icon] when the user picked one.
     */
    fun install(
        context: Context,
        loader: CleaveLoader,
        gameVersion: String?,
        loaderVersion: CleaveLoaderVersion?,
        name: String,
        icon: Bitmap?,
        onFinished: (Boolean) -> Unit
    ) {
        if (installing) return
        if (ProgressKeeper.hasOngoingTasks()) {
            Toast.makeText(context, R.string.tasks_ongoing, Toast.LENGTH_LONG).show()
            return
        }
        val appContext = context.applicationContext
        installing = true
        PojavApplication.sExecutorService.execute {
            val result = runCatching { performInstall(appContext, loader, gameVersion, loaderVersion, name.trim(), icon) }
            Tools.runOnUiThread {
                installing = false
                result.onSuccess { created ->
                    if (created) {
                        Toast.makeText(appContext, appContext.getString(R.string.cleave_create_done, name.trim()), Toast.LENGTH_SHORT).show()
                    }
                    PojavInstanceBridge.reload()
                    onFinished(created)
                }.onFailure {
                    Tools.showErrorRemote(it)
                    onFinished(false)
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun performInstall(
        context: Context,
        loader: CleaveLoader,
        gameVersion: String?,
        loaderVersion: CleaveLoaderVersion?,
        name: String,
        icon: Bitmap?
    ): Boolean {
        val prefix = directoryPrefix(name)
        val instance: Instance? = when (loader) {
            CleaveLoader.VANILLA -> {
                val version = gameVersion ?: return false
                Instances.createInstance({ i ->
                    i.name = name.ifEmpty { null }
                    i.versionId = version
                    i.icon = "vanilla"
                    i.sharedData = true
                }, prefix)
            }
            CleaveLoader.FABRIC, CleaveLoader.QUILT, CleaveLoader.LEGACY_FABRIC -> {
                val utils = when (loader) {
                    CleaveLoader.QUILT -> FabriclikeUtils.QUILT_UTILS
                    CleaveLoader.LEGACY_FABRIC -> FabriclikeUtils.LEGACY_FABRIC_UTILS
                    else -> FabriclikeUtils.FABRIC_UTILS
                }
                val version = gameVersion ?: return false
                val build = loaderVersion?.id ?: return false
                val versionId = utils.install(version, build)
                    ?: throw IOException(context.getString(R.string.fabric_dl_cant_read_meta, utils.name))
                Instances.createInstance({ i ->
                    i.name = name.ifEmpty { utils.name }
                    i.icon = if (loader == CleaveLoader.LEGACY_FABRIC) "legacy_fabric" else utils.iconName
                    i.versionId = versionId
                }, prefix.ifEmpty { versionId })
            }
            CleaveLoader.FORGE, CleaveLoader.NEOFORGE -> {
                val utils = if (loader == CleaveLoader.FORGE) ForgelikeUtils.FORGE_UTILS else ForgelikeUtils.NEOFORGE_UTILS
                val full = loaderVersion?.id ?: return false
                ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0)
                try {
                    val installer = utils.createInstaller(full)
                    val created = Instances.createInstance({ i ->
                        i.name = name.ifEmpty { utils.name }
                        i.icon = utils.iconName
                        i.installer = installer
                    }, prefix.ifEmpty { full })
                    ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
                    installer.start()
                    created
                } finally {
                    ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
                }
            }
            CleaveLoader.OPTIFINE -> {
                val version = loaderVersion?.source as? OptiFineUtils.OptiFineVersion ?: return false
                ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0)
                try {
                    OptiFineDownloadTask(version).prepareForInstall()
                    val installer = OptiFineUtils.createInstaller(version)
                    val created = Instances.createInstance({ i ->
                        i.name = name.ifEmpty { "OptiFine" }
                        // Without this the instance fell back to the stock icon and showed the
                        // launcher's logo instead of OptiFine's.
                        i.icon = "optifine"
                        i.installer = installer
                        i.sharedData = true
                    }, prefix.ifEmpty { "OptiFine" })
                    ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
                    installer.start()
                    created
                } finally {
                    ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK)
                }
            }
            CleaveLoader.BTA -> {
                val version = loaderVersion?.source as? BTAUtils.BTAVersion ?: return false
                runBta(version)
                // BTADownloadTask names the instance itself; apply the name typed on the page afterwards.
                val versionId = "bta-" + version.versionName
                val created = Instances.loadAllInstances().lastOrNull { it.versionId == versionId }
                if (created != null && name.isNotEmpty() && name != created.name) {
                    created.name = name
                    created.write()
                }
                created
            }
        }
        if (instance != null) {
            if (icon != null) {
                instance.encodeNewIcon(icon)
                InstanceIconProvider.dropIcon(instance)
            }
            // A brand new instance starts on the renderer its version calls for: LTW at 1.17 and
            // above, Emesis below it. Written onto the instance itself rather than left to the
            // global choice, so the instance keeps its own answer when the global renderer is
            // changed later, and the editor shows which one it is on.
            applyVersionRenderer(instance)
            Instances.setSelectedInstance(instance)
        }
        return instance != null
    }

    /**
     * Pin the renderer the instance's version should start on, when the instance does not already
     * carry an explicit choice.
     *
     * Forge and OptiFine install in the background and name the version they produce only when the
     * installer finishes; in that case there is no version id yet, and the launch path applies the
     * same rule from the version it ends up launching.
     */
    private fun applyVersionRenderer(instance: Instance) {
        if (!instance.renderer.isNullOrEmpty()) return
        val versionId = instance.versionId
        if (versionId.isNullOrEmpty() || versionId == Instance.VERSION_LATEST_RELEASE ||
            versionId == Instance.VERSION_LATEST_SNAPSHOT
        ) return
        val renderer = runCatching { RendererDefaults.launchTagForVersion(null, versionId) }.getOrNull()
        if (renderer.isNullOrEmpty()) return
        instance.renderer = renderer
        runCatching { instance.write() }
    }

    private fun runBta(version: BTAUtils.BTAVersion) {
        var failure: Exception? = null
        val listener = object : ModloaderDownloadListener {
            override fun onDownloadFinished(downloadedFile: File?) {}
            override fun onDataNotAvailable() {
                failure = IOException("BTA metadata is not available")
            }
            override fun onDownloadError(e: Exception) {
                failure = e
            }
        }
        BTADownloadTask(listener, version).run()
        failure?.let { throw it }
    }

    /** Directory prefix derived from the typed name; Instances appends a UUID so only safe characters are kept. */
    private fun directoryPrefix(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').take(48)
}
