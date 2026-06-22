tasks.withType<JavaCompile>().configureEach {
	enabled = false
}

tasks.withType<Javadoc>().configureEach {
	enabled = false
}

tasks.withType<Jar>().configureEach {
	enabled = false
}

tasks.named<Test>("test") {
	enabled = false
}
