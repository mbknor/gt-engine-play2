package kjetland.gtengineplay

import java.lang.String
import play.template2._
import compile.GTPreCompiler.{GTFragmentCode, SourceContext}
import exceptions.{GTRuntimeException, GTCompilationExceptionWithSourceInfo, GTRuntimeExceptionWithSourceInfo, GTTemplateNotFoundWithSourceInfo}
import play.template2.compile._
import compile.GTCompiler
import play.api.templates.Html
import play.data.Form
import play.api.i18n.Messages
import play.api.cache.Cache
import java.util.regex.{Pattern, Matcher}
import java.io._
import java.net.URL
import org.apache.commons.io.IOUtils
import play.api.{Play, PlayException}
import play.mvc.Http
import java.lang.reflect.Method


class RawData(val data : String)

abstract class GTJavaBase2xImpl(groovyClass: Class[_ <: GTGroovyBase], templateLocation: GTTemplateLocation) extends GTJavaBase(groovyClass, templateLocation) {

  var form: Option[Form[_ <: AnyRef]] = None

  def getRawDataClass = classOf[RawData]

  def convertRawDataToString(rawData: AnyRef) = {
    rawData match {
      case r : RawData => r.data
      case _ => throw new Exception("Not of type RawData")
    }
    
  }

  def escapeHTML(html: String) = org.apache.commons.lang.StringEscapeUtils.escapeHtml(html)

  def escapeXML(xml: String) = org.apache.commons.lang.StringEscapeUtils.escapeXml(xml)

  def escapeCsv(csv: String) = org.apache.commons.lang.StringEscapeUtils.escapeCsv(csv)

  def validationHasErrors(): Boolean = form.getOrElse( return false).hasErrors

  def validationHasError(name: String): Boolean = {

    val errors = form.getOrElse( return false).errors().get(name)
    if (errors == null || errors.size() == 0) {
      return false;
    } else {
      return true;
    }
  }

  override def resolveMessage(key: Any, args: Array[Object]): String = {
    Messages.apply(key.toString, args:_*)
  }

  def cacheGet(key: String) = {
    import play.api.Play.current
    Cache.get(key)
  }

  def cacheSet(key: String, data: AnyRef, duration: String) {
    import play.api.Play.current
    if (duration == null) {
      Cache.set(key.toString, data)
    } else {
      throw new Exception("Cache.set not implemented yet with duration string")
      //Cache.set(key, data, expiration)
    }
  }
}

class GTGroovyBase2xImpl extends GTGroovyBase {
  override def _resolveClass(clazzName: String): Class[_ <: Any] = {
    getClass.getClassLoader.loadClass(clazzName)
  }
}

object GTESettings {
  var controllersRoutesName = "controllers.routes";
}

class GTPreCompiler2xImpl(templateRepo: GTTemplateRepo) extends GTPreCompiler(templateRepo) {

  this.customFastTagResolver = GTFastTagResolver2xImpl

  override def getJavaBaseClass = classOf[GTJavaBase2xImpl]

  override def getGroovyBaseClass = classOf[GTGroovyBase2xImpl]

  // must modify all use of @{} in tag args
  override def checkAndPatchActionStringsInTagArguments(tagArgs : String) : String = {

    // It is tricky to support @@ - absolute urls since it is the tag itself that
    // calls url() or absoluteURL()
    // To go around this, we pass an extra parameter to the tag, '_use_absoluteURL=true' when @@ is used.
    // Then the tag can check for this param if needed

    

    val useAbsoluteUrl = tagArgs.indexOf("@@") >= 0
    var r = tagArgs
    if (useAbsoluteUrl) {
      r = r.replaceAll("""\@\@""", "_(\""+GTESettings.controllersRoutesName+"\").")
    }
     
    r = r.replaceAll("""\@""", "_(\""+GTESettings.controllersRoutesName+"\").")
    if (useAbsoluteUrl) {
      r = r + ",_use_absoluteURL:true"
    }
    r
  }

  val staticFileP = Pattern.compile("^'(.*)'$")

  override def generateRegularActionPrinter(absolute: Boolean, _action: String, sc: SourceContext, lineNo: Int): GTFragmentCode = {

    var code: String = null;
    var action = _action
    val m: Matcher = staticFileP.matcher(action.trim());
    if (m.find()) {
      // This is an action/link to a static file.
      action = m.group(1); // without ''
      if ( absolute) {
        code = " out.append("+GTESettings.controllersRoutesName+".Assets.at(\"" + action + "\").absoluteURL(play.mvc.Http.Context.current().request()));\n";
      } else {
        code = " out.append("+GTESettings.controllersRoutesName+".Assets.at(\"" + action + "\").url());\n";
      }
    } else {
      if (!action.endsWith(")")) {
        action = action + "()";
      }

      // generate groovy code
      sc.nextMethodIndex = sc.nextMethodIndex + 1
      val nextMethodIndex: Int = sc.nextMethodIndex
      val groovyMethodName: String = "action_resolver_" + nextMethodIndex;

      sc.gprintln(" String " + groovyMethodName + "() {", lineNo);
      sc.gprintln(" try {");
      if (absolute) {
        sc.gprintln(" return _(\""+GTESettings.controllersRoutesName+"\")." + action + ".absoluteURL(_(\"play.mvc.Http\\$Context\").current().request());");
      } else {
        sc.gprintln(" return _(\""+GTESettings.controllersRoutesName+"\")." + action + ".url();");
      }
      sc.gprintln("} catch(Exception e) {");
      sc.gprintln(" throw new play.template2.exceptions.GTTemplateRuntimeException(\"Error resolving route to action: \"+e.getClass().getName()+\": \"+e.getMessage())");
      sc.gprintln("}");
      sc.gprintln(" }");

      // generate java code that prints it
      code = " out.append(g." + groovyMethodName + "());";
    }

    return new GTFragmentCode(lineNo, code);
  }

}

class PreCompilerFactory extends GTPreCompilerFactory {
  def createCompiler(templateRepo: GTTemplateRepo) = new GTPreCompiler2xImpl(templateRepo)
}

class GTTypeResolver2xImpl extends GTTypeResolver {

  override def getTypeBytes(clazzName: String): Array[Byte] = {
    val name = clazzName.replace(".", "/") + ".class";
    val is: InputStream = getClass.getClassLoader.getResourceAsStream(name);
    if (is == null) {
      return null;
    }
    try {
      val os = new ByteArrayOutputStream();
      val buffer = new Array[Byte](8192);
      var count = 1;
      while (count > 0) {
        count = is.read(buffer, 0, buffer.length)
        if (count > 0) {
          os.write(buffer, 0, count);
        }
      }
      return os.toByteArray();
    } finally {
      is.close();
    }
  }
}

class GTFileResolver2xImpl() extends GTFileResolver.Resolver {

  private val USES_GROOVY_TEMPLATES_FILENAME = "uses-groovy-templates.txt"
  
  val gtviewRootURLs = findGTViewRootUrls()
  
  
  private def findGTViewRootUrls() : List[URL] = {
    import scala.collection.JavaConversions._

    this.getClass.getClassLoader.getResources(USES_GROOVY_TEMPLATES_FILENAME).toList.distinct.map( { f : URL =>
      // f is the location of the USES_GROOVY_TEMPLATES_FILENAME-file, we must create new url for the
      // actual template root "folder"
      val urlBase = f.toString.substring(0, f.toString.lastIndexOf("/"))
      // TODO: Use filter instead
      // read the file to find all root folders
      val lines : Array[String] = IOUtils.toString( f.openStream(), "utf-8").split("\\r?\\n")
      lines.filterNot( _.trim().startsWith("#")).map( {path : String =>
        new URL(urlBase + path.trim())
      })
    }).flatten
  }
  
  private def urlWorks(url : URL) : Boolean = {
    try {
      url.openStream().close()
      return true;
    } catch {
      case _ => return false
    }
  }

  def getTemplateLocationReal(queryPath: String): GTTemplateLocationReal = {

    for (i <- 0 until gtviewRootURLs.size ) {
      val rootUrl = gtviewRootURLs(i)
      val url = new URL(rootUrl.toString + queryPath)
      if ( urlWorks(url)) {
        return new GTTemplateLocationReal(url.toString, url)
      }
    }
    return null
  }

  def getTemplateLocationFromRelativePath(relativePath: String): GTTemplateLocationReal = {
    val url = new URL(relativePath)
    if ( urlWorks(url)) {
      return new GTTemplateLocationReal(url.toString, url)
    } else {
      return null;
    }
  }
}

object GTJavaExtensionMethodResolver2impl extends GTJavaExtensionMethodResolver {
  private val GT_JAVA_EXTENSIONS_FILENAME = "gt-java-extensions.txt"
  val methodName2ClassMapping : Map[String, Class[_]] = {
    
    import scala.collection.JavaConversions._
    // Find all JavaExtension-classes
    val allClasses = this.getClass.getClassLoader.getResources(GT_JAVA_EXTENSIONS_FILENAME).toList.map( { f : URL =>
      val lines : Array[String] = IOUtils.toString( f.openStream(), "utf-8").split("\\r?\\n")
      lines.filterNot( _.trim().startsWith("#")).map( {clazzName : String =>
        this.getClass.getClassLoader.loadClass(clazzName.trim())
      }).toList
    }).flatten

    // find all methods
    allClasses.map( {c: Class[_] => c.getDeclaredMethods.map( (_.getName -> c) ) }).flatten.toMap
  }
  
  
  def findClassWithMethod(methodName: String) : Class[_] = {
    methodName2ClassMapping.getOrElse(methodName, null)
  }
}

class GTETemplate(gtJavaBase: GTJavaBase2xImpl) {

  protected var allParams : Map[String,  AnyRef] = Map()
  protected var form : Option[Form[_ <: AnyRef]] = None
  
  def withForm(form : Form[_ <: AnyRef]) : GTETemplate = {
    this.form = Some(form)
    this
  }
  
  def addParams(params: java.util.Map[String, AnyRef]): GTETemplate = {
    import scala.collection.JavaConversions._
    allParams = allParams++params
    this
  }

  def addParams(params: Map[String, AnyRef]): GTETemplate = {
    allParams = allParams++params
    this
  }
  
  def addParam(name : String,  value : AnyRef) : GTETemplate = {
    allParams = allParams+(name->value)
    this
  }

  def render(params: java.util.Map[String, AnyRef]): Html = {
    import scala.collection.JavaConversions._
    allParams = allParams++params
    _render()
  }

  def render(params: Map[String, AnyRef]): Html = {
    allParams = allParams++params
    _render()
  }

  def render(): Html = {
      _render()
    }

  private def _render(): Html = {
    import scala.collection.JavaConversions._
    
    gtJavaBase.form = this.form
    
    // add system params
    if ( !form.isEmpty ) {
      allParams = allParams + ("_form" -> form.get)
    }
    allParams = allParams ++ Map(
      ("_response_encoding" -> "utf-8"),
      ("play" -> Play),
      ("messages" -> Messages),
      ("flash", Http.Context.current().flash()),
      ("session", Http.Context.current().session()),
      ("request", Http.Context.current().request()),
      ("response", Http.Context.current().response())
    )



    
    gteHelper.exceptionTranslator( { () =>
      gtJavaBase.renderTemplate(allParams)
    })

    new Html(gtJavaBase.getAsString) // TODO: Can be optimized to not return string just yet..
  }
}

class GTJavaExtensionMethodResolver2xImpl extends GTJavaExtensionMethodResolver {
  def findClassWithMethod(methodName: String) = null
}

object gte {

  if ( Play.maybeApplication.isDefined ) {
    GTCompiler.srcDestFolder = Play.maybeApplication.get.configuration.getString("gte-generated-src-path") match {
      case Some(e:String) => new File(e)
      case None => null
    }
  }

  val parentClassLoader: ClassLoader = getClass.getClassLoader

  GTGroovyPimpTransformer.gtJavaExtensionMethodResolver = GTJavaExtensionMethodResolver2impl
  GTJavaCompileToClass.typeResolver = new GTTypeResolver2xImpl()
  GTGroovyPimpTransformer.gtJavaExtensionMethodResolver = new GTJavaExtensionMethodResolver2xImpl

  GTFileResolver.impl = new GTFileResolver2xImpl();

  val folderToDumpClassesIn : File = null //new File("tmp/gttemplates");
  val repo = new GTTemplateRepo(parentClassLoader, true, new PreCompilerFactory, false, folderToDumpClassesIn)

  def template(path: String): GTETemplate = {
    gteHelper.exceptionTranslator({ () =>
      val gtJavaBase: GTJavaBase = repo.getTemplateInstance( GTFileResolver.impl.getTemplateLocationReal(path))
      new GTETemplate(gtJavaBase.asInstanceOf[GTJavaBase2xImpl])
    })
  }

}

object GTFastTagResolver2xImpl extends GTFastTagResolver {

  val fastTagResolvers = getFastTagResolvers


  def resolveFastTag(tagName: String) : String = {
    
    for (i <- 0 until fastTagResolvers.size ) {
      val resolver = fastTagResolvers(i)
      val fastTag = resolver.resolveFastTag(tagName)
      if ( fastTag != null) {
        return fastTag
      }
    }

    return null
  }

  private def getFastTagResolvers : List[GTFastTagResolver] = {
    val GT_FASTTAGS_FILENAME = "gt-fasttags.txt"
    import scala.collection.JavaConversions._
    this.getClass.getClassLoader.getResources(GT_FASTTAGS_FILENAME).toList.map( { f : URL =>
      val lines : Array[String] = IOUtils.toString( f.openStream(), "utf-8").split("\\r?\\n")
      lines.filterNot( _.trim().startsWith("#")).map( {clazzName : String =>
        this.getClass.getClassLoader.loadClass(clazzName.trim()).asInstanceOf[Class[GTFastTagResolver]].newInstance()
      })
      }).flatten
  }

}


object gteHelper {
  
  def exceptionTranslator[T](code: () => T) : T = {
      try {
  
        code()
  
      } catch {
        case e: GTTemplateNotFoundWithSourceInfo => {
          import scalax.io._
          throw new PlayException(
            "Template not found",
            "[%s: %s]".format("Template not found", e.queryPath),
            Some(e)) with PlayException.ExceptionSource {
            def line = Some(e.lineNo)
  
            def position = None
  
            def input = Some(Resource.fromInputStream(new ByteArrayInputStream(e.templateLocation.readSource().getBytes("utf-8"))))
  
            def sourceName = Some(e.templateLocation.relativePath)
          }
        }
        case e: GTRuntimeExceptionWithSourceInfo => {
          import scalax.io._
          throw new PlayException(
            "Template Runtime Exception",
            "[%s: %s]".format(e.getCause.getClass.getSimpleName, e.getCause.getMessage),
            Some(e.getCause)) with PlayException.ExceptionSource {
            def line = Some(e.lineNo)
  
            def position = None
  
            def input = Some(Resource.fromInputStream(new ByteArrayInputStream(e.templateLocation.readSource().getBytes("utf-8"))))
  
            def sourceName = Some(e.templateLocation.relativePath)
          }
        }
        case e: GTCompilationExceptionWithSourceInfo => {
          import scalax.io._
          throw new PlayException(
            "Template Compilation Error",
            "[%s: %s]".format(e.getClass.getSimpleName, e.getMessage),
            Some(e)) with PlayException.ExceptionSource {
            def line = Some(e.oneBasedLineNo)
  
            def position = None
  
            def input = Some(Resource.fromInputStream(new ByteArrayInputStream(e.templateLocation.readSource().getBytes("utf-8"))))
  
            def sourceName = Some(e.templateLocation.relativePath)
          }
        }
        case e: GTRuntimeException => {
          val cause: Throwable = if (e.getCause == null) {
            e
          } else {
            e.getCause()
          }
          import scalax.io._
          throw new PlayException(
            "Template Runtime Exception",
            "[%s: %s]".format(cause.getClass.getSimpleName, cause.getMessage),
            Some(e.getCause)) with PlayException.ExceptionSource {
            def line = None
  
            def position = None
  
            def input = None
  
            def sourceName = None
          }
        }
      }
  }

}