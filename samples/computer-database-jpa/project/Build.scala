import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "computer-database-jpa"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
      "kjetland" %% "gt-engine-play2" % "0.1.6"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      ebeanEnabled := false,
      resolvers ++= Seq(
            Resolver.file("Local ivy Repository", file("/Users/mortenkjetland/.ivy2/local/"))(Resolver.ivyStylePatterns),
            "mbknor github Repository" at "http://mbknor.github.com/m2repo/releases/"
            )   
    )

}
            
