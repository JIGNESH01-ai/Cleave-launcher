package net.kdt.pojavlaunch.cleave.bridge

import com.google.gson.JsonArray

/** Alternatives in one Modrinth facet group; version and loader stay separate AND groups. */
internal fun discoveryCategoryFacet(categories: List<String>): String = JsonArray().apply {
    categories.filter { it.isNotBlank() }.distinct().forEach { add("categories:$it") }
}.toString()
