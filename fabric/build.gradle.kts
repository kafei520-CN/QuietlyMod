import net.fabricmc.loom.task.RemapJarTask

plugins {
	id("fabric-loom")
}

dependencies {
	minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
	compileOnly("net.fabricmc:sponge-mixin:0.15.3+mixin.0.8.7")
}

loom {
	splitEnvironmentSourceSets()
	mixin.useLegacyMixinAp = true
	mixin.defaultRefmapName.set("quietly.refmap.json")

	runs {
		named("client") {
			client()
			runDir("../run/fabric-client")
			configName = "Fabric Client"
		}
		named("server") {
			server()
			runDir("../run/fabric-server")
			configName = "Fabric Server"
		}
	}
}

tasks.named<JavaCompile>("compileJava") {
	source(project(":common").sourceSets.main.get().allJava)
}

tasks.withType<Jar>().configureEach {
	destinationDirectory.set(rootDir.resolve("build").resolve("libs_fabric"))
}

tasks.withType<RemapJarTask>().configureEach {
	destinationDirectory.set(rootDir.resolve("build").resolve("libs_fabric"))
}

tasks.javadoc {
	source(project(":common").sourceSets.main.get().allJava)
}

tasks.processResources {
	from(project(":common").sourceSets.main.get().resources)

	val propertyMap = mapOf(
		"version" to project.version.toString(),
		"mod_id" to rootProject.property("mod_id").toString(),
		"mod_name" to rootProject.property("mod_name").toString(),
		"mod_description" to rootProject.property("mod_description").toString(),
		"mod_author" to rootProject.property("mod_author").toString(),
		"loader_version" to rootProject.property("loader_version").toString(),
		"minecraft_versions" to rootProject.property("compatible_minecraft_versions_fabric").toString()
	)
	inputs.properties(propertyMap)

	filesMatching("fabric.mod.json") {
		expand(propertyMap)
	}
}

tasks.named<Test>("test") {
	enabled = false
}
