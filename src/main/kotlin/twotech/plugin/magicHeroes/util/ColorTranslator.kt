package twotech.plugin.magicHeroes.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.regex.Pattern

/**
 * Utility class to translate color codes in text to Adventure Components
 */
object ColorTranslator {
    // Regex matching hex color codes like &#ffffff
    private val HEX_PATTERN: Pattern = Pattern.compile("&#([A-Fa-f0-9]{6})")

    /**
     * Translates string message containing '&' and '&#ffffff' to Adventure Component
     *
     * @param message Message string to translate
     * @return Colored text Component
     */
    fun translate(message: String): Component {
        var translated = message
        val matcher = HEX_PATTERN.matcher(translated)
        
        // Convert &#ffffff to legacy hex format: &x&f&f&f&f&f&f
        while (matcher.find()) {
            val color = matcher.group(1)
            val replacement = buildString {
                append("&x")
                for (char in color) {
                    append("&").append(char)
                }
            }
            translated = translated.replace(matcher.group(0), replacement)
        }
        
        // Use LegacyComponentSerializer to deserialize ampersand and translated hex color codes
        return LegacyComponentSerializer.legacyAmpersand().deserialize(translated)
            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)
    }
}
