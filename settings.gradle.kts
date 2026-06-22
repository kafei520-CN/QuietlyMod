pluginManagement {
	val loomVersion = providers.gradleProperty("loom_version").get()
	val forgeGradleVersion = providers.gradleProperty("forge_gradle_version").get()

	plugins {
		id("fabric-loom") version loomVersion
		id("net.minecraftforge.gradle") version forgeGradleVersion
	}

	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
			content {
				includeGroupByRegex("net\\.fabricmc(\\..*)?")
				includeGroup("fabric-loom")
			}
		}
		maven {
			name = "Sponge"
			url = uri("https://repo.spongepowered.org/repository/maven-public/")
			content {
				includeGroupByRegex("org\\.spongepowered(\\..*)?")
			}
		}
		maven {
			name = "MinecraftForge"
			url = uri("https://maven.minecraftforge.net/")
			content {
				includeGroupByRegex("net\\.minecraftforge(\\..*)?")
				includeGroupByRegex("cpw\\.mods(\\..*)?")
				includeGroup("de.oceanlabs.mcp")
				includeGroup("net.minecraft")
				includeGroup("net.minecraftforge.gradle")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "quietly"
include("common", "fabric", "forge")
