package net.kdt.pojavlaunch.cleave.ui.screens

import net.kdt.pojavlaunch.cleave.bridge.*
import net.kdt.pojavlaunch.cleave.ui.LauncherSection
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class DiscoverProjectRequestTest {
    @After fun clear() { CleaveDownloadRequest.consumeProject(); CleaveDownloadRequest.type = null }
    @Test fun cataloguePinsOnlyTheSuppliedFabricProject() {
        val entry = teamReleases.single()
        assertEquals("1749327", entry.projectId)
        assertEquals("aetherium-fabric", entry.slug)
        assertEquals(AssetType.MOD, entry.type)
        assertTrue(entry.warning.contains("OptiFine"))
    }
    @Test fun exactProjectIsSentToDiscoverNotAnExternalDownload() {
        val project = AssetProject(AssetPlatform.CURSEFORGE, AssetType.MOD, "123", "Fixture", "", "", null, 0)
        var destination: LauncherSection? = null
        CleaveDownloadRequest.openProject(project) { destination = it }
        assertEquals(LauncherSection.DISCOVER, destination)
        assertSame(project, CleaveDownloadRequest.project)
        assertNull(CleaveDownloadRequest.type)
        CleaveDownloadRequest.consumeProject()
        assertNull(CleaveDownloadRequest.project)
    }
    @Test fun categoryRequestReplacesAnOldProjectRequest() {
        CleaveDownloadRequest.openProject(AssetProject(AssetPlatform.CURSEFORGE, AssetType.MOD, "123", "", "", "", null, 0)) {}
        CleaveDownloadRequest.open(AssetType.RESOURCE_PACK) {}
        assertNull(CleaveDownloadRequest.project)
        assertEquals(AssetType.RESOURCE_PACK, CleaveDownloadRequest.type)
    }
}
