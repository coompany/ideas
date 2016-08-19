package auth

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider._


class ApiTokenEndpoint extends TokenEndpoint {

    override val handlers: Map[String, GrantHandler] = Map(
        OAuthGrantType.IMPLICIT -> new Implicit {
            override def clientCredentialRequired: Boolean = false

            override def handleRequest[U](request: AuthorizationRequest, handler: AuthorizationHandler[U])(implicit ctx: ExecutionContext): Future[GrantHandlerResult[U]] = {
                val implicitRequest = ImplicitRequest(request)
                val clientCredential = implicitRequest.clientCredential.getOrElse(throw new InvalidRequest("Client credential is required"))

                handler.findUser(implicitRequest).flatMap { maybeUser =>
                    val user = maybeUser.getOrElse(throw new InvalidGrant("user cannot be authenticated"))
                    val scope = implicitRequest.scope
                    val authInfo = AuthInfo(user, Some(clientCredential.clientId), scope, implicitRequest.param("redirect_uri"))

                    issueAccessToken(handler, authInfo)
                }
            }
        }
    )

}
