package auth

import scalaoauth2.provider._


class ApiTokenEndpoint extends TokenEndpoint {

    override val handlers: Map[String, GrantHandler] = Map(
        OAuthGrantType.IMPLICIT -> new Implicit {
            override def clientCredentialRequired: Boolean = false
        }
    )

}
