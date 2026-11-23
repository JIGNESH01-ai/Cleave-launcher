package net.kdt.pojavlaunch.cleave.client

import java.io.File

/** Local IPC for optional client-only sky/weather presentation. */
object ClientVisualBridge {
    fun sync(gameDir: File) {
        val file = File(gameDir, "config/cleave_visual.properties")
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, "cleave_visual.properties.tmp")
        val nextStamp = maxOf(System.currentTimeMillis(), file.lastModified() + 1L)
        try {
            temp.writeText("time=${CleaveClientStore.visualTimeMode}\nweather=${CleaveClientStore.visualWeatherMode}\n")
            check(temp.renameTo(file)) { "Could not save visual settings" }
            file.setLastModified(nextStamp)
        } finally { temp.delete() }
    }
}
