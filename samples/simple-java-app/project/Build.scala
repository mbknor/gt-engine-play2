import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "simple-java-app"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "kjetland" %% "gt-engine-play" % "0.1"
    )
    
    javacOptions += "-g"

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers ++= Seq("mbknor github Repository" at "http://mbknor.github.com/m2repo/releases/")
    )

}
