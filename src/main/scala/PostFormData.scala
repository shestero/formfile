import akka.http.scaladsl.model.ContentType
import akka.util.ByteString

case class PostFormData(fields: Map[String, String], files: Map[String, FormFileData]) {

  private def filePart(
                        fieldName: String,
                        fileName: String,
                        contentType: ContentType,
                        data: ByteString
                      ): FormFileData =
    files.get(fieldName) match {
      case None => FormFileData(fileName, contentType, data)
      case Some(continue) => continue.next(fileName, contentType, data)
    }

  def addFilePart(
                   fieldName: String,
                   fileName: String,
                   contentType: ContentType
                 )(data: ByteString): PostFormData = {
    println(s"addFilePart: $fieldName $fileName $contentType ...")
    val part = filePart(fieldName, fileName, contentType, data)
    copy(files = files ++ Map(fieldName -> part))
  }

  def addField(name: String, value: String): PostFormData =
    copy(fields = fields ++ Map(name -> value))

  override def toString(): String = {
     "Fields:\n" + fields.map { case (name, value) => s"\t$name: $value" }.mkString("\n") + "\n" +
     "Files:\n" + files.map { case (name, value) => s"\t$name: $value" }.mkString("\n")
  }
}

object PostFormData {
  def empty: PostFormData = PostFormData(Map.empty, Map.empty)
}