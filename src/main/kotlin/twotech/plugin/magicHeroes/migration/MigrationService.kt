package twotech.plugin.magicHeroes.migration

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class MigrationService(private val plugin: JavaPlugin) {
    fun migrateLegacyFiles() {
        migrateFile(File(plugin.dataFolder, "classes.yml"))
        migrateFolder(File(plugin.dataFolder, "playerdata"))
        migrateFolder(File(plugin.dataFolder, "tooltips"))
        migrateFolder(File(plugin.dataFolder, "lang"))
    }

    private fun migrateFolder(folder: File) {
        folder.listFiles { file -> file.extension == "yml" }?.forEach(::migrateFile)
        folder.listFiles(File::isDirectory)?.forEach(::migrateFolder)
    }

    private fun migrateFile(file: File) {
        if (!file.exists()) return
        val configuration = YamlConfiguration.loadConfiguration(file)
        if (configuration.getInt("schema-version", 0) >= 1) return
        val backup = File(file.parentFile, "${file.name}.v0.bak")
        if (!backup.exists()) Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES)
        configuration.set("schema-version", 1)
        configuration.save(file)
        plugin.logger.info("Migrated ${file.relativeTo(plugin.dataFolder)}")
    }
}
