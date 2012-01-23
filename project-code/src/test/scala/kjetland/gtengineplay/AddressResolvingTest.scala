package kjetland.gtengineplay

import org.specs2.mutable._
import play.template2.GTTemplateLocationWithEmbeddedSource
import play.template2.exceptions.{GTRuntimeExceptionWithSourceInfo, GTTemplateRuntimeException, GTRuntimeException}
import play.api.mvc.Call

object MyRoutes {

  def index() = Call("GET", "/index")
  def index2(a:Int, b:Int) = Call("GET", "/index2")
}


object AddressResolvingTest extends Specification with renderer {


  GTESettings.controllersRoutesName = "kjetland.gtengineplay.MyRoutes"

  "Address resolving" should {
    "be valid with no params" in {
      render("@{index()}") must beEqualTo("/index")
    }
    
    "be valid with params" in {
      render("@{index2(1,2)}") must beEqualTo("/index2")
    }

    "fail when address does not exists" in {
      render("@{notExisting()}") must throwA[GTRuntimeExceptionWithSourceInfo].like {case e => e.getMessage must startWith("Error resolving route to action:")}
    }

    "fail when using wrong number of args" in {
      render("@{index2(1)}") must throwA[GTRuntimeExceptionWithSourceInfo].like {case e => e.getMessage must startWith("Error resolving route to action:")}
    }

    "fail when using wrong type of args" in {
      render("""@{index2(1,"A")}""") must throwA[GTRuntimeExceptionWithSourceInfo].like {case e => e.getMessage must startWith("Error resolving route to action:")}
    }


  }

  "Address resolving in tag-args" should {
      "be valid for working actions" in {
        render("#{form @index(), something: 123/}") must startWith("<form action=\"/index\" method=\"get\"")
      }

    "be valid for working actions with params" in {
      render("#{form @index2(1,2), something: 123/}") must startWith("<form action=\"/index2\" method=\"get\"")
    }

// TODO: Need to wrap address-resolving in tags in a function so that we can try-catch it..
//    "fail when address does not exists" in {
//      render("#{form @notExisting(1,2), something: 123/}") must throwA[GTRuntimeExceptionWithSourceInfo].like {case e => e.getMessage must startWith("Error resolving route to action:")}
//    }


  }


}

trait renderer {
  def render( source : String) : String = {
    val tl = new GTTemplateLocationWithEmbeddedSource(source)
    val t = gte.repo.getTemplateInstance(tl)
    import scala.collection.JavaConversions._
    t.renderTemplate(Map[String, Object]())
    t.getAsString
  }
}