import sbt._
import Keys._

object GTEnginePlayBuild extends Build {

  val mbknorGithubRepoUrl = "http://mbknor.github.com/m2repo/releases/"


  lazy val GTEnginePlayProject = Project(
    "gt-engine-play",
    new File("."),
    settings = BuildSettings.buildSettings ++ Seq(
                libraryDependencies := Dependencies.runtime,
                publishMavenStyle := true,
                publishTo := Some(Resolvers.mbknorRepository),
                scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8"),
                javacOptions ++= Seq("-encoding", "utf8", "-g"),
                resolvers ++= Seq(DefaultMavenRepository, Resolvers.mbknorGithubRepo)
            )
        )


  object Resolvers {
      val mbknorRepository = Resolver.ssh("my local mbknor repo", "localhost", "~/projects/mbknor.github.com/m2repo/releases/")(Resolver.mavenStylePatterns)
      val mbknorGithubRepo = "mbknor github Repository" at mbknorGithubRepoUrl
  }

  object Dependencies {

      val runtime = Seq(
        "kjetland" % "gt-engine_2.9.1" % "0.1.7"
      )
  }


  object BuildSettings {

          val buildOrganization = "kjetland"
          val buildVersion      = "0.1"
          val buildScalaVersion = "2.9.1"
          val buildSbtVersion   = "0.11.2"

          val buildSettings = Defaults.defaultSettings ++ Seq (
              organization   := buildOrganization,
              version        := buildVersion,
              scalaVersion   := buildScalaVersion
          )

      }


}