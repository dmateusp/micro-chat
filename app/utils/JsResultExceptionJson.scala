package utils

import play.api.data.validation._
import play.api.libs.json._

object JsResultExceptionJson {
  val toJson : JsResultException => JsValue =
    (e) => Json.obj("error" -> replaceEscapes(e.errors.head._2.head.message))

  // replaces \" to "
  private val replaceEscapes : String => String = (s) => s.replaceAll("\\\"", "\"")
}
