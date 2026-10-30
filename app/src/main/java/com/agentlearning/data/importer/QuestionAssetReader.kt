package com.agentlearning.data.importer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class QuestionBankParser @Inject constructor(private val json: Json) {
    fun parseManifest(text: String): ManifestDto = json.decodeFromString(text)
    fun parseWeek(text: String): WeekFileDto = json.decodeFromString(text)

    /** Loads a week's main file and merges the topics and questions of its extra files. */
    fun parseWeekFiles(entry: ManifestWeekDto, read: (String) -> String): WeekFileDto {
        val main = parseWeek(read(entry.file))
        return entry.extraFiles.fold(main) { merged, name ->
            val extra = parseWeek(read(name))
            if (extra.week != entry.week) {
                throw QuestionBankException(listOf("$name declares week ${extra.week}, expected ${entry.week}"))
            }
            merged.copy(topics = merged.topics + extra.topics, questions = merged.questions + extra.questions)
        }
    }
}

class QuestionAssetReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: QuestionBankParser,
) {
    fun loadManifest(): ManifestDto = parser.parseManifest(read(MANIFEST))

    fun loadAll(manifest: ManifestDto = loadManifest()): QuestionBank =
        QuestionBank(manifest, manifest.weeks.map { parser.parseWeekFiles(it, ::read) })

    private fun read(name: String): String =
        context.assets.open("$DIR/$name").bufferedReader(Charsets.UTF_8).use { it.readText() }

    companion object {
        const val DIR = "questions"
        const val MANIFEST = "manifest.json"
    }
}
