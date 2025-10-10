import org.gradle.kotlin.dsl.get
import java.io.PrintWriter
import kotlin.collections.containsKey
import kotlin.collections.putAll

val baseLocale = "values"
val targetLocales = listOf(
    "values-ru", "values-fr", "values-es", "values-de", "values-it",
    "values-ar-rSA", "values-bg", "values-zh", "values-cs",
    "values-pt-rBR", "values-lo", "values-pl", "values-hy", "values-si", "values-nl"
)

/**
 * Extracts the application version from build configuration files.
 * This version will be used in translation comments to track when strings were added.
 * The code tries multiple patterns and files to find version information.
 */
val appVersion = try {
    val rootProject = project.rootProject
    val buildGradle = file("${rootProject.projectDir}/appmanager/build.gradle.kts")
    if (buildGradle.exists()) {
        val content = buildGradle.readText()
        val patterns = listOf(
            "versionName\\s+[\"']([^\"']+)[\"']",     // versionName "1.2.3"
            "versionName\\s+=\\s+[\"']([^\"']+)[\"']", // versionName = "1.2.3"
            "versionName\\s+([\\d.]+)",               // versionName 1.2.3
            "ext\\.versionName\\s*=\\s*[\"']([^\"']+)[\"']" // ext.versionName = "1.2.3"
        )

        var version: String? = null
        for (pattern in patterns) {
            val match = pattern.toRegex().find(content)
            if (match != null && match.groupValues.size > 1) {
                version = match.groupValues[1]
                break
            }
        }

        if (version == null) {
            val gradleProperties = file("${rootProject.projectDir}/gradle.properties")
            if (gradleProperties.exists()) {
                val propertiesContent = gradleProperties.readText()
                val propertyMatch = "version\\s*=\\s*([\\d.]+)".toRegex().find(propertiesContent)
                if (propertyMatch != null && propertyMatch.groupValues.size > 1) {
                    version = propertyMatch.groupValues[1]
                }
            }
        }

        version ?: "unknown"
    } else {
        println("Warning: app/build.gradle not found")
        "unknown"
    }
} catch (e: Exception) {
    println("Error retrieving version: ${e.message}")
    "unknown"
}

/**
 * Task: extractTranslations
 *
 * Extracts untranslated strings from all project modules and creates XML files with missing translations.
 * The task analyzes all string resources in the project modules and identifies strings that are present
 * in the base locale but missing in target locales. It generates XML files for each language with all
 * missing strings, organized by module.
 *
 * Options:
 * - fillWithOriginals: When true, missing translations are pre-filled with the original text.
 *                     When false, empty strings are generated. Default is true.
 *
 * Output:
 * - Creates [lang]-missing-translations.xml files in the translations directory
 * - Creates original-strings.xml with all original strings for reference
 */
tasks.register("extractTranslations") {
    group = "translations"
    description = "Extracts untranslated strings from all project modules"

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

                    addTranslationEntries(writer, strings)
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

                addTranslationEntries(writer, strings)
            }

            writer.println("</resources>")
            println("- Created file with original strings: ${originalFile.name}")
        }

        println("\nDone! Results saved in directory: $outputDir")
        println("Total files created: ${missingTranslations.count { it.value.isNotEmpty() } + 1}")
    }
}

/**
 * Task: importXmlTranslations
 *
 * Imports translations from XML files back into project modules.
 * The task reads translation files generated by extractTranslations, and adds the translated strings
 * to the appropriate resource files in each module. It preserves existing translations and appends
 * new ones with version and date comments.
 *
 * The task handles:
 * - Detection of proper target file (strings.xml or translatable.xml)
 * - Adding version and date information in XML comments
 * - Creating new resource files if they don't exist
 * - Appending to existing files while preserving their content
 *
 * Input:
 * - XML files in the translations directory with naming pattern [lang]-missing-translations.xml
 */
tasks.register("importXmlTranslations") {
    group = "translations"
    description = "Imports translations from XML files back into project modules"

    doLast {
        val inputDir = "${project.buildDir}/../../translations"
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
        println("Current app version: $appVersion")

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

                // Определяем, куда импортировать строки
                val targetFile = file("${targetDir}/strings.xml")
                val targetTranslatableFile = file("${targetDir}/translatable.xml")

                val baseTranslatableFile = file("${moduleProject.projectDir}/src/main/res/values/translatable.xml")
                val useTranslatableFile = baseTranslatableFile.exists() && strings.any { key ->
                    val baseTranslations = if (baseTranslatableFile.exists()) parseStringXml(baseTranslatableFile) else emptyMap()
                    baseTranslations.containsKey(key.key)
                }

                val fileToUpdate = if (useTranslatableFile) targetTranslatableFile else targetFile

                // Разделяем строки на обычные и plurals
                val regularStrings = mutableMapOf<String, String>()
                val pluralStrings = mutableMapOf<String, String>()

                strings.filter { (_, value) -> value.isNotEmpty() }.forEach { (key, value) ->
                    if (key.startsWith("plurals:")) {
                        pluralStrings[key.removePrefix("plurals:")] = value
                    } else {
                        regularStrings[key] = value
                    }
                }

                if (regularStrings.isEmpty() && pluralStrings.isEmpty()) {
                    println("    - No filled strings to import")
                    return@forEach
                }

                if (fileToUpdate.exists()) {
                    val existingContent = fileToUpdate.readText()
                    val closingTagIndex = existingContent.lastIndexOf("</resources>")

                    if (closingTagIndex != -1) {
                        fileToUpdate.printWriter().use { writer ->
                            writer.print(existingContent.substring(0, closingTagIndex))

                            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
                            val currentDate = dateFormat.format(java.util.Date())
                            writer.println("\n    <!-- Newly added translations (v$appVersion - $currentDate) -->")

                            // Добавляем обычные строки
                            regularStrings.forEach { (key, value) ->
                                writer.println("    <string name=\"$key\">$value</string>")
                            }

                            // Добавляем plurals в правильном формате
                            pluralStrings.forEach { (key, value) ->
                                writer.println("    <plurals name=\"$key\">")

                                // Проверяем содержит ли значение уже теги item
                                if (value.contains("<item quantity=")) {
                                    // Вставляем item-строки напрямую с правильными отступами
                                    value.lines().forEach { line ->
                                        val trimmedLine = line.trim()
                                        if (trimmedLine.isNotEmpty()) {
                                            writer.println("        $trimmedLine")
                                        }
                                    }
                                } else {
                                    // Если это просто текст, то добавляем как одно значение
                                    writer.println("        <item quantity=\"other\">$value</item>")
                                }

                                writer.println("    </plurals>")
                            }

                            // Закрываем тег resources
                            writer.println("</resources>")
                        }
                    } else {
                        println("    - Warning: Could not find closing resources tag in existing file")
                        return@forEach
                    }
                } else {
                    fileToUpdate.printWriter().use { writer ->
                        writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                        writer.println("<resources>")

                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
                        val currentDate = dateFormat.format(java.util.Date())
                        writer.println("    <!-- Translations added in version $appVersion - $currentDate -->")

                        // Добавляем обычные строки
                        regularStrings.forEach { (key, value) ->
                            writer.println("    <string name=\"$key\">$value</string>")
                        }

                        // Добавляем plurals в правильном формате
                        pluralStrings.forEach { (key, value) ->
                            writer.println("    <plurals name=\"$key\">")

                            if (value.contains("<item quantity=")) {
                                // Вставляем item-строки напрямую с правильными отступами
                                value.lines().forEach { line ->
                                    val trimmedLine = line.trim()
                                    if (trimmedLine.isNotEmpty()) {
                                        writer.println("        $trimmedLine")
                                    }
                                }
                            } else {
                                // Если это просто текст, то добавляем как одно значение
                                writer.println("        <item quantity=\"other\">$value</item>")
                            }

                            writer.println("    </plurals>")
                        }

                        writer.println("</resources>")
                    }
                }

                println("    - Updated file: ${fileToUpdate.name} (added ${regularStrings.size} strings, ${pluralStrings.size} plurals)")
            }
        }

        println("\nTranslation import completed.")
    }
}

/**
 * Helper function: parseStringXml
 *
 * Parses a standard Android string resource XML file.
 * Extracts all translatable string resources with their keys and values,
 * including plurals with their different quantity forms.
 * Ignores strings with translatable="false" attribute.
 *
 * @param file The Android string resource XML file to parse
 * @return A map of string resource names to their values
 */
fun parseStringXml(file: File): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val content = file.readText()

    // Process regular strings - ignore strings with translatable="false"
    val stringRegex = "<string\\s+name=\"([^\"]+)\"([^>]*)>(.*?)</string>".toRegex(RegexOption.DOT_MATCHES_ALL)
    val stringMatches = stringRegex.findAll(content)

    for (match in stringMatches) {
        val key = match.groupValues[1]
        val attributes = match.groupValues[2]
        val value = match.groupValues[3]
            .trim()
            .replace("\n\\s*".toRegex(), " ")

        // Skip strings marked as non-translatable
        if (attributes.contains("translatable=\"false\"")) {
            continue
        }

        result[key] = value
    }

    // Process plurals
    val pluralsRegex = "<plurals\\s+name=\"([^\"]+)\"[^>]*>(.*?)</plurals>".toRegex(RegexOption.DOT_MATCHES_ALL)
    val pluralsMatches = pluralsRegex.findAll(content)

    for (pluralMatch in pluralsMatches) {
        val pluralKey = pluralMatch.groupValues[1]
        val pluralContent = pluralMatch.groupValues[2]

        // Store the entire content of the plurals tag
        result["plurals:$pluralKey"] = pluralContent.trim()
    }

    return result
}

/**
 * Helper function: parseModuleTranslations
 *
 * Parses an XML file with translations organized by modules.
 * Extracts strings and plurals from files generated by extractTranslations task.
 *
 * @param file The XML file to parse
 * @return A map where keys are module names and values are maps of string key-value pairs
 */
fun parseModuleTranslations(file: File): Map<String, Map<String, String>> {
    val result = mutableMapOf<String, MutableMap<String, String>>()
    var currentModule = ""

    val content = file.readText()

    val moduleRegex = "\\s*<!-- Module: ([\\w-]+) .*-->\\s*".toRegex()
    val moduleMatches = moduleRegex.findAll(content)

    for (moduleMatch in moduleMatches) {
        val moduleName = moduleMatch.groupValues[1]
        val moduleStart = moduleMatch.range.last + 1

        // Find end of current module (start of next module or end of file)
        val nextModuleMatch = moduleRegex.find(content, moduleStart)
        val moduleEnd = nextModuleMatch?.range?.first ?: content.length

        val moduleContent = content.substring(moduleStart, moduleEnd)
        val moduleMap = result.getOrPut(moduleName) { mutableMapOf() }

        // Find regular strings in this module
        val stringRegex = "\\s*<string\\s+name=\"([^\"]+)\"(?:[^>]*)>(.*?)</string>\\s*".toRegex(RegexOption.DOT_MATCHES_ALL)
        val stringMatches = stringRegex.findAll(moduleContent)
        for (stringMatch in stringMatches) {
            val key = stringMatch.groupValues[1]
            val value = stringMatch.groupValues[2]
                .trim()
                .replace("\n\\s*".toRegex(), " ")

            // Проверяем, является ли это plural в формате string
            if (key.startsWith("plurals:")) {
                // Сохраняем value как есть для правильной обработки позже
                moduleMap[key] = value
            } else {
                // Обычные строки
                moduleMap[key] = value
            }
        }

        // Find plurals in this module (стандартные plurals теги)
        val pluralsRegex = "\\s*<plurals\\s+name=\"([^\"]+)\"[^>]*>(.*?)</plurals>\\s*".toRegex(RegexOption.DOT_MATCHES_ALL)
        val pluralsMatches = pluralsRegex.findAll(moduleContent)

        for (pluralMatch in pluralsMatches) {
            val pluralKey = pluralMatch.groupValues[1]
            val pluralContent = pluralMatch.groupValues[2]

            // Store with the plurals: prefix
            moduleMap["plurals:$pluralKey"] = pluralContent.trim()
        }
    }

    return result
}

/**
 * Helper function: addTranslationEntries
 *
 * Adds translation entries to the given writer, handling both regular strings and plurals.
 */
fun addTranslationEntries(writer: PrintWriter, strings: Map<String, String>) {
    strings.forEach { (key, value) ->
        if (key.startsWith("plurals:")) {
            // Process plurals
            val pluralKey = key.substring("plurals:".length)
            writer.println("    <plurals name=\"$pluralKey\">")

            // The value could be in two formats:
            // 1. Already in XML format with <item quantity="one">...</item>
            // 2. JSON format converted from XML

            if (value.contains("<item")) {
                // If it already contains XML item tags, use it directly
                value.trim().lines().forEach { line ->
                    writer.println("        $line")
                }
            } else {
                // Try to parse as JSON format
                try {
                    // Simple parsing for JSON format like {"one":"1 item","other":"%d items"}
                    val itemRegex = "\"([^\"]+)\":\"([^\"]+)\"".toRegex()
                    val items = itemRegex.findAll(value)

                    items.forEach { itemMatch ->
                        val quantity = itemMatch.groupValues[1]
                        val itemValue = itemMatch.groupValues[2]
                        writer.println("        <item quantity=\"$quantity\">$itemValue</item>")
                    }
                } catch (e: Exception) {
                    println("    - Error processing plural: $key, $value")
                    // Fallback: just add as a comment if parsing fails
                    writer.println("        <!-- Failed to parse plural: $value -->")
                }
            }

            writer.println("    </plurals>")
        } else {
            // Regular strings
            writer.println("    <string name=\"$key\">$value</string>")
        }
    }
}