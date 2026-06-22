plugins {
	id("java")
	id("idea")
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "idea")
	apply(plugin = "maven-publish")

	val baseModVersion = rootProject.property("mod_version").toString()
	val minecraftSuffix = rootProject.property("mod_version_minecraft_suffix").toString()
	version = when (project.name) {
		"fabric" -> "${baseModVersion}+fabric${minecraftSuffix}"
		"forge" -> "${baseModVersion}+forge${minecraftSuffix}"
		else -> baseModVersion
	}
	group = rootProject.property("maven_group").toString()

	base {
		archivesName.set(rootProject.property("mod_id").toString())
	}

	repositories {
		mavenCentral()
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
			}
		}
	}

	java {
		withSourcesJar()
		toolchain.languageVersion.set(JavaLanguageVersion.of(rootProject.property("java_version").toString().toInt()))
	}

	tasks.withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
		options.release.set(rootProject.property("java_version").toString().toInt())
	}

	tasks.withType<Jar>().configureEach {
		val projectName = project.name
		inputs.property("projectName", projectName)
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
		from(rootProject.file("LICENSE")) {
			rename { "${it}_$projectName" }
		}
	}
}

tasks.jar {
	enabled = false
}
