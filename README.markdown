Groovy Template Engine for Play 2
=========

This project is a module to Play 2. It uses the gt-engine (https://github.com/mbknor/gt-engine) to enable the same Groovy Templates from Play 1 in Play 2.
More info about gt-engine can be found here: http://kjetland.com/blog/2011/11/playframework-new-faster-groovy-template-engine/

Due to lots of SBT-integration problems, I did not integrate gt-engine into Play 2's SBT code. This means that the groovy template files
are not compiled when SBT compiles your applications. Instead, they are compiled on the fly when used the first time.

---------------

Please have a look at the sample project - https://github.com/mbknor/gt-engine-play2/tree/master/samples/computer-database-jpa - to see how to use it.

This is what you have to do to enable it:

Add dependency to the module using sbt (project/Build.scala):

	val appDependencies = Seq(
	  "kjetland" %% "gt-engine-play" % "0.1.1"
	)

You also have to tell sbt where to find it:

	val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
					resolvers ++= Seq("mbknor github Repository" at "http://mbknor.github.com/m2repo/releases/")   
				)

Then you have to create a file in /conf/ named 'uses-groovy-templates.txt' with this content:

	# All apps/jars containing groovy templates must have this file with the name 'uses-groovy-templates.txt'
	# on the root of its classpath.
	#
	# Then gte can find all such files on the classpath and then locate
	# all locations where groovy templates might be found.
	#
	# Below you must list all "root folders" where template files can be found
	/gtviews/
	
the folder 'gtviews/' must me on the root of the classpath - so it gets included in jars etc..
For now, place this folder in the '/conf/'-folder like this: '/conf/gtviews/' - this is where you place all your groovy template files

In your controller, you do it like this:

	package controllers;
	
	...
	import kjetland.gtengineplay.gte;
	...
	
	public class Application extends Controller {
		public static Result index() {
	        Form<Data> form = form(Data.class).bindFromRequest();

	        return ok( gte.template("Application/input1.html")
	                .withForm(form)
	                .addParam("myString", "This is a string")
	                .render()
					);
	    }

	}



---------------

This is working:
----------------

 * Basic template stuff
 * all tags (#extends, #include, #list, etc)
 * resolving values ($)
 * resolving messages (&)
 * Checking for errors
 * Using cache
 * Resolving addresses (@)
 * Error messges showing correct source and lineno in templates
 * Finding template files from disk
 * custom tags
 * FastTag-handling-integration
 * loading templates from modules (jars)
 * resolving template files via resources
 * system params (_encoding, all the params that are auto added by play 1)
 * Code in FastTag that was not moved from play 1 to gt-engine
 * JavaExtensions
 * Resolving absolute addresses (@@)



This is still not working (Works like a todo list):
------------

 * Improve routes-resolving in tag-args by wrapping in method to try-catch it - needs "inteligent" parsing..
 * move gtviews out of conf-folder
 * Scala support (scala uses different Form-instance)
 * render args
 * i18n.tag
 * Locale support


----------


Documentation hints:

When using GTFastTags, you must have a file called gt-fasttags.txt located on the "root" of your classpath (you can put it in the /conf-folder)
Each fasttag must have its full classname on a seperate line in this file.

The same goes for JavaExtensions: The file should be named 'gt-java-extensions.txt'

Must have 'uses-groovy-templates.txt'-file on root of classpath in all apps/jars that contain groovy templates.
The template source root folder should be named /gtviews and should also be on the root of the classpath, for example in the conf folder.