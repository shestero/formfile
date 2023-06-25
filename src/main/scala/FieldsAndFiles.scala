import akka.http.scaladsl.model.{ContentType, HttpEntity}
import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object FieldsAndFiles {

  private def sink(implicit mat: Materializer, ec: ExecutionContext) =
    Sink.foldAsync[PostFormData, FormData.BodyPart](PostFormData.empty) {
      case (result, part) =>
        part.filename.map { fileName =>
          part.entity.dataBytes.runFold(result)(_.addFilePart(part.name, fileName, part.entity.contentType)(_))
        } getOrElse {
          part.entity match {
            case HttpEntity.Strict(ct, data) if ct.isInstanceOf[ContentType.NonBinary] =>
              val charsetName = ct.asInstanceOf[ContentType.NonBinary].charset.nioCharset.name
              val partContent = data.decodeString(charsetName)
              Future.successful( result.addField(part.name, partContent) )
            case _ => // part of unknown kind // discard(part)
              part.entity.discardBytes().future.map(_ => result) // drain
          }
        }
    }

  def withFormContent: Directive1[Future[PostFormData]] =
    entity(as[FormData]).flatMap{ formData =>
      extractMaterializer.flatMap { implicit mat: Materializer =>
        extractExecutionContext.map { implicit ecx: ExecutionContextExecutor =>
          formData.parts.runWith(sink)
        }
      }
    }

}
