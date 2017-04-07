lazy val commonSettings = Seq(
  organization := "com.xxx.yyy",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  publishMavenStyle := false,
  crossPaths := false,
  autoScalaLibrary := false
)

packSettings //pack sbt plugin
packMain := Map("app1" -> "com.com.xxx.yyy.App1")
packJvmOpts := Map("app1" -> Seq("-Dparam0=param0Value -Dparam1=param1Value"))

lazy val myproject = (project in file("myproject")).
  settings(commonSettings: _*).
  settings(
    name := "myproject",
    libraryDependencies ++= Seq(
      "mysql" % "mysql-connector-java" % "5.1.40",
      "ch.qos.logback" % "logback-classic" % "1.1.8",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.8.5",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.5",
      "io.ebean" % "ebean" % "10.1.4",
      "io.ebean" % "ebean-agent" % "10.1.2"
    ),
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "com/xxx/yyy/**"
    )
  )

//from playframework
import sbt.inc.Analysis
def enhanceEbeanClasses(classpath: Classpath, analysis: Analysis, classDirectory: File, pkg: String): Analysis = {
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl.loadClass("io.ebean.enhance.agent.Transformer").getConstructor(classOf[Array[URL]], classOf[String]).newInstance(cp, "debug=0").asInstanceOf[AnyRef]
  val ft = cl.loadClass("io.ebean.enhance.ant.OfflineFileTransform").getConstructor(
    t.getClass, classOf[ClassLoader], classOf[String]
  ).newInstance(t, ClassLoader.getSystemClassLoader, classDirectory.getAbsolutePath).asInstanceOf[AnyRef]
  ft.getClass.getDeclaredMethod("process", classOf[String]).invoke(ft, pkg)
  analysis
}
