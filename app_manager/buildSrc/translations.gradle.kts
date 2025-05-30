import org.gradle.kotlin.dsl.get
import kotlin.collections.containsKey
import kotlin.collections.putAll

val baseLocale = "values"
val targetLocales = listOf(
    "values-ru", "values-fr", "values-es", "values-de", "values-it",
    "values-ar-rSA", "values-bg", "values-zh", "values-cs",
    "values-pt-rBR", "values-lo", "values-pl", "values-hy", "values-si"
)

//
tasks.register("extractTranslations") {
    group = "translations"
    description = "Extracts untranslated strings from all project modules"

    // Добавляем свойство для управления заполнением
    val fillWithOriginals = project.findProperty("fillWithOriginals")?.toString()?.toBoolean() ?: true

    doLast {
        println("Extracting untranslated strings. Fill with original text: $fillWithOriginals")

        val outputDir = "${project.buildDir}/../../translations"
        file(outputDir).mkdirs()

        val missingTranslations = mutableMapOf<String, MutableMap<String, MutableMap<String, String>>>()
        val originalTexts = mutableMapOf<String, MutableMap<String, String>>()

        targetLocales.forEach { locale ->
            val normalizedLocale = locale.removePrefix("values-")
            missingTranslations[normalizedLocale] = mutableMapOf()
        }

        println("Searching for untranslated strings...")

        project.subprojects.forEach { subproject ->
            println("Checking module: ${subproject.name}")
            originalTexts[subproject.name] = mutableMapOf()

            val resDir = file("${subproject.projectDir}/src/main/res")
            if (!resDir.exists()) {
                println("  - Resource directory not found: $resDir")
                return@forEach
            }

            val baseStringsFile = file("${resDir}/$baseLocale/strings.xml")
            val baseTranslatableFile = file("${resDir}/$baseLocale/translatable.xml")

            val baseStrings = mutableMapOf<String, String>()

            if (baseStringsFile.exists()) {
                baseStrings.putAll(parseStringXml(baseStringsFile))
                println("  - Found strings.xml file with ${baseStrings.size} strings")
            }

            if (baseTranslatableFile.exists()) {
                baseStrings.putAll(parseStringXml(baseTranslatableFile))
                println("  - Found translatable.xml file, added strings: ${parseStringXml(baseTranslatableFile).size}")
            }

            if (baseStrings.isEmpty()) {
                println("  - No strings found for localization")
                return@forEach
            }

            baseStrings.forEach { (key, value) ->
                originalTexts[subproject.name]!![key] = value
            }

            targetLocales.forEach { targetLocale ->
                val normalizedLocale = targetLocale.removePrefix("values-")
                val targetStringsDir = file("${resDir}/$targetLocale")

                if (!targetStringsDir.exists()) {
                    val moduleStrings = missingTranslations[normalizedLocale]!!.getOrPut(subproject.name) { mutableMapOf() }

                    baseStrings.forEach { (key, value) ->
                        moduleStrings[key] = if (fillWithOriginals) value else ""
                    }
                    println("  - $targetLocale: localization directory is missing, added ${baseStrings.size} strings")
                    return@forEach
                }

                val targetStrings = mutableMapOf<String, String>()
                val targetStringsFile = file("${targetStringsDir}/strings.xml")
                val targetTranslatableFile = file("${targetStringsDir}/translatable.xml")

                if (targetStringsFile.exists()) {
                    targetStrings.putAll(parseStringXml(targetStringsFile))
                }

                if (targetTranslatableFile.exists()) {
                    targetStrings.putAll(parseStringXml(targetTranslatableFile))
                }

                val moduleStrings = missingTranslations[normalizedLocale]!!.getOrPut(subproject.name) { mutableMapOf() }

                baseStrings.forEach { (key, value) ->
                    if (!targetStrings.containsKey(key)) {
                        moduleStrings[key] = if (fillWithOriginals) value else ""
                    }
                }

                println("  - $targetLocale: found ${moduleStrings.size} untranslated strings")
            }
        }

        missingTranslations.forEach { (lang, moduleMap) ->
            if (moduleMap.isEmpty() || moduleMap.all { it.value.isEmpty() }) {
                println("No untranslated strings for language $lang")
                return@forEach
            }

            val xmlFile = file("$outputDir/$lang-missing-translations.xml")
            xmlFile.printWriter().use { writer ->
                writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                writer.println("<resources>")
                writer.println("    <!-- Missing translations for language: $lang -->")

                moduleMap.keys.sorted().forEach { module ->
                    val strings = moduleMap[module] ?: mutableMapOf()
                    if (strings.isEmpty()) return@forEach

                    writer.println("\n    <!-- Module: $module - ${strings.size} strings -->")

                    strings.keys.sorted().forEach { key ->
                        val value = strings[key]!!.replace("\"", "\\\"").replace("'", "\\'")
                        writer.println("    <string name=\"$key\">$value</string>")
                    }
                }

                writer.println("</resources>")
            }

            println("- Created file for language $lang: ${xmlFile.name} with ${moduleMap.values.sumOf { it.size }} strings")
        }

        val originalFile = file("$outputDir/original-strings.xml")
        originalFile.printWriter().use { writer ->
            writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            writer.println("<resources>")
            writer.println("    <!-- Original strings for reference -->")

            originalTexts.keys.sorted().forEach { module ->
                val strings = originalTexts[module] ?: mutableMapOf()
                if (strings.isEmpty()) return@forEach

                writer.println("\n    <!-- Module: $module - ${strings.size} strings -->")

                strings.keys.sorted().forEach { key ->
                    val value = strings[key]!!.replace("\"", "\\\"").replace("'", "\\'")
                    writer.println("    <string name=\"$key\">$value</string>")
                }
            }

            writer.println("</resources>")
            println("- Created file with original strings: ${originalFile.name}")
        }

        println("\nDone! Results saved in directory: $outputDir")
        println("Total files created: ${missingTranslations.count { it.value.isNotEmpty() } + 1}")
    }
}

tasks.register("importXmlTranslations") {
    group = "translations"
    description = "Imports translations from XML files back into project modules"

    doLast {
        val inputDir = "${project.buildDir}/translations"
        val translationsDir = file(inputDir)

        if (!translationsDir.exists()) {
            println("Translations directory not found: $inputDir")
            return@doLast
        }

        val xmlFiles = translationsDir.listFiles { file ->
            file.isFile && file.name.endsWith("-missing-translations.xml")
        } ?: emptyArray()

        if (xmlFiles.isEmpty()) {
            println("No XML translation files found in directory: $inputDir")
            return@doLast
        }

        println("Found ${xmlFiles.size} translation files")

        xmlFiles.forEach { xmlFile ->
            val lang = xmlFile.name.substringBefore("-missing-translations.xml")

            println("Processing language: $lang")
            val translations = parseModuleTranslations(xmlFile)

            translations.forEach { (module, strings) ->
                if (strings.isEmpty()) {
                    println("  - Module $module: no strings to import")
                    return@forEach
                }

                val moduleProject = project.subprojects.find { it.name == module }
                if (moduleProject == null) {
                    println("  - Module not found: $module")
                    return@forEach
                }

                println("  - Importing into module $module: ${strings.size} strings")

                val targetDir = file("${moduleProject.projectDir}/src/main/res/values-$lang")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                // Determine whether to import to strings.xml or translatable.xml
                val targetFile = file("${targetDir}/strings.xml")
                val targetTranslatableFile = file("${targetDir}/translatable.xml")

                val baseTranslatableFile = file("${moduleProject.projectDir}/src/main/res/values/translatable.xml")
                val useTranslatableFile = baseTranslatableFile.exists() && strings.any { key ->
                    val baseTranslations = if (baseTranslatableFile.exists()) parseStringXml(baseTranslatableFile) else emptyMap()
                    baseTranslations.containsKey(key.key)
                }

                val fileToUpdate = if (useTranslatableFile) targetTranslatableFile else targetFile

                val existingStrings = if (fileToUpdate.exists()) {
                    parseStringXml(fileToUpdate).toMutableMap()
                } else {
                    mutableMapOf()
                }

                val stringsToAdd = strings.filter { (_, value) -> value.isNotEmpty() }

                if (stringsToAdd.isEmpty()) {
                    println("    - No filled strings to import")
                    return@forEach
                }

                existingStrings.putAll(stringsToAdd)

                fileToUpdate.printWriter().use { writer ->
                    writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                    writer.println("<resources>")

                    existingStrings.keys.sorted().forEach { key ->
                        val value = existingStrings[key]!!
                        writer.println("    <string name=\"$key\">$value</string>")
                    }

                    writer.println("</resources>")
                }

                println("    - Updated file: ${fileToUpdate.name} (added ${stringsToAdd.size} strings)")
            }
        }

        println("\nTranslation import completed.")
    }
}

fun parseModuleTranslations(file: File): Map<String, Map<String, String>> {
    val result = mutableMapOf<String, MutableMap<String, String>>()
    var currentModule = ""

    val lines = file.readLines()

    val moduleRegex = "\\s*<!-- Module: ([\\w-]+) .*-->\\s*".toRegex()
    val stringRegex = "\\s*<string name=\"([^\"]+)\">([^<]*)</string>\\s*".toRegex()

    for (line in lines) {
        val moduleMatch = moduleRegex.matchEntire(line)
        if (moduleMatch != null) {
            currentModule = moduleMatch.groupValues[1]
            result.putIfAbsent(currentModule, mutableMapOf())
            continue
        }

        if (currentModule.isNotEmpty()) {
            val stringMatch = stringRegex.matchEntire(line)
            if (stringMatch != null) {
                val key = stringMatch.groupValues[1]
                val value = stringMatch.groupValues[2]
                result[currentModule]!![key] = value
            }
        }
    }

    return result
}

fun parseStringXml(file: File): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val content = file.readText()

    val regex = "<string name=\"([^\"]+)\">([^<]+)</string>".toRegex()
    val matches = regex.findAll(content)

    for (match in matches) {
        val key = match.groupValues[1]
        val value = match.groupValues[2]
        result[key] = value
    }

    return result
}