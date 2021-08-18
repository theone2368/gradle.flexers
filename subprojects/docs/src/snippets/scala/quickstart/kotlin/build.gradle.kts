// tag::use-plugin[]
plugins {
    // end::use-plugin[]
    eclipse
// tag::use-plugin[]
    scala
}
// end::use-plugin[]

// tag::scala-dependency[]
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.11.12")
    testImplementation("org.scalatest:scalatest_2.11:3.0.0")
    testImplementation("junit:junit:4.13")
}
// end::scala-dependency[]


// tag::scala3-dependency[]
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.0.1")
    testImplementation("org.scalatest:scalatest_3:3.2.9")
    testImplementation("junit:junit:4.13")
}
// end::scala3-dependency[]

dependencies {
    implementation("commons-collections:commons-collections:3.2.2")
}
