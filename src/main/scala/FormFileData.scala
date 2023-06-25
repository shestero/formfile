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

  def check(fileName: String, contentType: ContentType) = {
    assert(fileName==this.fileName)
    assert(contentType==this.contentType)
  }

  def accept(data: ByteString): FormFileData =
  {
    println(s"processing part ${data.length}")
    digest.update(data.asByteBuffer)
    this
  }

  def out: String = {
    val hash = String.format("%032X", new BigInteger(1, digest.digest())).toLowerCase
    s"$hash $fileName ($contentType)"
  }
}

object FormFileData {
  // for first part
  def apply(fileName: String,
            contentType: ContentType,
            data: ByteString
           ): FormFileData =
    FormFileData(fileName, contentType).accept(data)
}