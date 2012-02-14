import sbt._
import Keys._

object GTEnginePlayBuild extends Build {

  val mbknorGithubRepoUrl = "http://mbknor.github.com/m2repo/releases/"
  val typesafeRepoUrl = "http://repo.typesafe.com/typesafe/releases/"

  lazy val GTEnginePlayProject = Project(
    "gt-engine-play2",
    new File("."),
    settings = BuildSettings.buildSettings ++ Seq(
                libraryDependencies := Dependencies.runtime,
                publishMavenStyle := true,
                publishTo := Some(Resolvers.mbknorRepository),
                scalacOptions ++= Seq("-Xlint","-deprecation", "-unchecked","-encoding", "utf8"),
                javacOptions ++= Seq("-encoding", "utf8", "-g"),
                resolvers ++= Seq(DefaultMavenRepository, Resolvers.mbknorGithubRepo, Resolvers.typesafe)
            )
        )


  object Resolvers {
      val mbknorRepository = Resolver.ssh("my local mbknor repo", "localhost", "~/projects/mbknor.github.com/m2repo/releases/")(Resolver.mavenStylePatterns)
      val mbknorGithubRepo = "mbknor github Repository" at mbknorGithubRepoUrl
      val typesafe = "Typesafe Repository" at typesafeRepoUrl
  }

  object Dependencies {

      val runtime = Seq(
        "kjetland"     %    "gt-engine_2.9.1"      % "0.1.7.8",
        "play"         %%   "play"                 % "2.0-RC1" % "provided" notTransitive(),
        "play"         %%   "templates"            % "2.0-RC1" % "provided" notTransitive(),
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.2.0" % "provided",
        "commons-io"   %    "commons-io"           % "2.0.1",
        "org.specs2"   %%   "specs2"               % "1.6.1"              %  "test",
        "com.novocode" %    "junit-interface"      % "0.8"                %  "test"

      )
  }


  object BuildSettings {

          val buildOrganization = "kjetland"
          val buildVersion      = "0.1.3"
          val buildScalaVersion = "2.9.1"
          val buildSbtVersion   = "0.11.2"

          val buildSettings = Defaults.defaultSettings ++ Seq (
              organization   := buildOrganization,
              version        := buildVersion,
              scalaVersion   := buildScalaVersion
          )

      }


}