import akka.http.scaladsl.model.ContentType
import akka.util.ByteString
import java.math.BigInteger
import java.security.MessageDigest

case class FormFileData(
                         fileName: String,
                         contentType: ContentType,
                         digest: MessageDigest = MessageDigest.getInstance("MD5")
                       )
{

  // first peace of content of each file comes here; you may create on disk file here
  protected def create(data: ByteString): FormFileData = append(data)

  // every next parts comes here
  protected def append(data: ByteString): FormFileData =
  {
    println(s"processing part ${data.length}")
    digest.update(data.asByteBuffer)
    this
  }

  def next(fileName: String, contentType: ContentType, data: ByteString): FormFileData = {
    assert(fileName==this.fileName)
    assert(contentType==this.contentType)
    append(data)
  }

  def out: String = {
    val hash = String.format("%032X", new BigInteger(1, digest.digest())).toLowerCase
    s"$hash $fileName ($contentType)"
  }
}

object FormFileData {
  // for first part
  def apply(fileName: String, contentType: ContentType, data: ByteString): FormFileData =
    FormFileData(fileName, contentType).create(data)
}