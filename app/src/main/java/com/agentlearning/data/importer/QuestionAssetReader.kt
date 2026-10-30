package com.agentlearning.data.importer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuestionBankParser @Inject constructor(private val json: Json) {
    fun parseManifest(text: String): ManifestDto = json.decodeFromString(text)
    fun parseWeek(text: String): WeekFileDto = json.decodeFromString(text)
}

class QuestionAssetReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: QuestionBankParser,
) {
    fun loadManifest(): ManifestDto = parser.parseManifest(read(MANIFEST))

    fun loadAll(manifest: ManifestDto = loadManifest()): QuestionBank =
        QuestionBank(manifest, manifest.weeks.map { parser.parseWeek(read(it.file)) })

    private fun read(name: String): String =
        context.assets.open("$DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }

    companion object {
        const val DIR = "questions"
        const val MANIFEST = "manifest.json"
    }
}
