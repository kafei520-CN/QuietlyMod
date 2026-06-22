import org.spongepowered.asm.gradle.plugins.MixinExtension

buildscript {
	repositories {
		maven {
			name = "Sponge"
			url = uri("https://repo.spongepowered.org/repository/maven-public/")
		}
	}
	dependencies {
		classpath("org.spongepowered:mixingradle:0.7.38")
	}
}

plugins {
	id("net.minecraftforge.gradle")
}

apply(plugin = "org.spongepowered.mixin")

dependencies {
	minecraft("net.minecraftforge:forge:${rootProject.property("forge_version")}")
	annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

minecraft {
	mappings("official", rootProject.property("minecraft_version").toString())
	copyIdeResources = true
	
	runs {
		create("client") {
			workingDirectory(rootProject.file("run/forge-client"))
			property("forge.logging.markers", "REGISTRIES")
			property("forge.logging.console.level", "debug")
			mods {
				create(rootProject.property("mod_id").toString()) {
					source(sourceSets.main.get())
				}
			}
		}
		create("server") {
			workingDirectory(rootProject.file("run/forge-server"))
			property("forge.logging.markers", "REGISTRIES")
			property("forge.logging.console.level", "debug")
			args("--nogui")
			mods {
				create(rootProject.property("mod_id").toString()) {
					source(sourceSets.main.get())
				}
			}
		}
	}
}

extensions.configure<MixinExtension>("mixin") {
	add(sourceSets.main.get(), "quietly.refmap.json")
	config("quietly.mixins.json")
}

tasks.jar {
	destinationDirectory.set(rootDir.resolve("build").resolve("libs_forge"))
	manifest.attributes(
		mapOf(
			"MixinConfigs" to "quietly.mixins.json"
		)
	)
}

tasks.withType<JavaCompile>().configureEach {
	source(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().configureEach {
	source(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<ProcessResources>().configureEach {
	from(project(":common").sourceSets.main.get().resources)

	val propertyMap = mapOf(
		"mod_id" to rootProject.property("mod_id").toString(),
		"mod_name" to rootProject.property("mod_name").toString(),
		"mod_version" to project.version.toString(),
		"mod_description" to rootProject.property("mod_description").toString(),
		"mod_author" to rootProject.property("mod_author").toString(),
		"mod_license" to rootProject.property("mod_license").toString(),
		"loader_version_range" to rootProject.property("forge_loader_version_range").toString(),
		"minecraft_version_range" to rootProject.property("compatible_minecraft_versions_forge").toString()
	)
	inputs.properties(propertyMap)

	filesMatching("META-INF/mods.toml") {
		expand(propertyMap)
	}
}

tasks.named("compileTestJava") {
	enabled = false
}

tasks.named<Test>("test") {
	enabled = false
}
