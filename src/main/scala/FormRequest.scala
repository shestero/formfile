import akka.http.scaladsl.model.ContentType
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import scala.util.chaining._

case class FormRequest(fields: Map[String, String], files: Map[String, FormFileData]) {

  private def filePart(
                        fieldName: String,
                        fileName: String,
                        contentType: ContentType,
                        data: ByteString
                      ): FormFileData =
    files.get(fieldName) match {
      case None => FormFileData(fileName, contentType, data)
      case Some(continue) =>
        continue.check(fileName, contentType)
        continue.accept(data)
    }

  def addFilePart(
                   fieldName: String,
                   fileName: String,
                   contentType: ContentType
                 )(data: ByteString): FormRequest = {
    println(s"addFilePart: $fieldName $fileName $contentType ...")
    val part = filePart(fieldName, fileName, contentType, data)
    copy(files = files ++ Map(fieldName -> part))
  }

  def addField(name: String, value: String): FormRequest =
    copy(fields = fields ++ Map(name -> value))

  def out: String = {
     "Fields:\n" + fields.map { case (name, value) => s"\t$name: $value" }.mkString("\n") + "\n" +
     "Files:\n" + files.map { case (name, value) => s"\t$name: ${value.out}" }.mkString("\n")
  }
}

object FormRequest {
  def empty: FormRequest = FormRequest(Map.empty, Map.empty)
}