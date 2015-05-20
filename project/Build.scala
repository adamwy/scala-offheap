import sbt._, Keys._
import pl.project13.scala.sbt.SbtJmh._

object RegionsBuild extends Build {
  val paradiseVersion = "2.1.0-M5"
  val defaults = Defaults.defaultSettings ++ Seq(
    scalaVersion := "2.11.6",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    initialCommands in console += "import offheap._; implicit val alloc = Allocator()",
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  )

  lazy val root = Project(
    "root",
    file("."),
    settings = defaults,
    aggregate = Seq(core, macros)
  )

  lazy val core = Project(
    "core",
    file("core"),
    settings = defaults ++ Seq(
      //scalacOptions += "-Xprint:cleanup"
    ),
    dependencies = Seq(macros)
  )

  lazy val macros = Project(
    "macros",
    file("macros"),
    settings = defaults ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= (
        if (!scalaVersion.value.startsWith("2.10")) Nil
        else List("org.scalamacros" %% "quasiquotes" % paradiseVersion)
      )
    )
  )

  lazy val sandbox = Project(
    "sandbox",
    file("sandbox"),
    settings = defaults ++ Seq(
      incOptions := incOptions.value.withNameHashing(false),
      scalacOptions += "-Xprint:typer",
      fork in run := true,
      javaOptions in run ++= Seq("-Xms256m", "-Xmx256m")
    ),
    dependencies = Seq(macros, core)
  )

  lazy val tests = Project(
    "tests",
    file("tests"),
    settings = defaults ++ Seq(
      libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
      libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
      incOptions := incOptions.value.withNameHashing(false),
      parallelExecution in Test := false,
      fork in Test := true
    ),
    dependencies = Seq(core, macros)
  )

  lazy val jmh = Project(
    "jmh",
    file("jmh"),
    settings = defaults ++ jmhSettings,
    dependencies = Seq(core)
  )
}
