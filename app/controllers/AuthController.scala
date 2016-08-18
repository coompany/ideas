package controllers

import javax.inject.{Inject, Singleton}

import auth.DefaultEnv
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasher}
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Controller, Result}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps


@Singleton
class AuthController @Inject() (
    silhouette: Silhouette[DefaultEnv],
    val messagesApi: MessagesApi,
    userService: UserService,
    passwordHasher: PasswordHasher,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    configuration: Configuration,
    clock: Clock) extends Controller with I18nSupport {

    def getSignUp = silhouette.UserAwareAction.async { implicit request =>
        redirectIfLoggedIn(request, Future.successful(Ok(views.html.signUp(SignUpForm.form))))
    }

    def postSignUp = silhouette.UserAwareAction.async { implicit request =>
        redirectIfLoggedIn(request, {
            SignUpForm.form.bindFromRequest.fold(
                formWithErrors =>
                    Future.successful(BadRequest(views.html.signUp(formWithErrors))),
                userData => {
                    val loginInfo = LoginInfo(CredentialsProvider.ID, userData.email)
                    userService.retrieve(loginInfo) flatMap {
                        case Some(existingUser) =>
                            Future.successful(Redirect(routes.AuthController.getSignUp()).flashing("error" -> Messages("error.userExists")))
                        case None =>
                            val authInfo = passwordHasher.hash(userData.password)
                            val user = User(
                                id = 0,
                                firstName = userData.firstName,
                                lastName = userData.lastName,
                                email = userData.email
                            )

                            for {
                                user <- userService.save(user)
                                authInfo <- authInfoRepository.add(loginInfo, authInfo)
                                authenticator <- silhouette.env.authenticatorService.create(loginInfo)
                                value <- silhouette.env.authenticatorService.init(authenticator)
                                result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.HomeController.index()))
                            } yield {
                                silhouette.env.eventBus.publish(SignUpEvent(user, request))
                                silhouette.env.eventBus.publish(LoginEvent(user, request))
                                result
                            }
                    }
                }
            )
        })
    }

    def getSignIn = silhouette.UserAwareAction.async { implicit request =>
        redirectIfLoggedIn(request, Future.successful(Ok(views.html.signIn(SignInForm.form))))
    }

    def postSignIn = silhouette.UserAwareAction.async { implicit request =>
        redirectIfLoggedIn(request, {
            SignInForm.form.bindFromRequest.fold(
                formWithErrors =>
                    Future.successful(BadRequest(views.html.signIn(formWithErrors))),
                loginData => {
                    val credentials = Credentials(loginData.email, loginData.password)
                    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
                        val result = Redirect(routes.HomeController.index())
                        userService.retrieve(loginInfo).flatMap {
                            case Some(user) =>
                                val c = configuration.underlying
                                silhouette.env.authenticatorService.create(loginInfo).map {
                                    case authenticator if loginData.rememberMe =>
                                        authenticator.copy(
                                            expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                                            idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                                            cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                                        )
                                    case authenticator => authenticator
                                }.flatMap { authenticator =>
                                    silhouette.env.eventBus.publish(LoginEvent(user, request))
                                    silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                                        silhouette.env.authenticatorService.embed(v, result)
                                    }
                                }
                            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
                        }
                    }
                }
            )
        })
    }

    private def redirectIfLoggedIn[B](request: UserAwareRequest[DefaultEnv, B], action: Future[Result]): Future[Result] = {
        request.identity match {
            case Some(user) => Future.successful(Redirect(routes.HomeController.index()))
            case _ => action
        }
    }
}


object SignUpForm {

    val form = Form(
        mapping(
            "firstName" -> nonEmptyText,
            "lastName" -> nonEmptyText,
            "email" -> email,
            "password" -> nonEmptyText
        )(Data.apply)(Data.unapply)
    )

    case class Data (firstName: String,
                     lastName: String,
                     email: String,
                     password: String)

}


object SignInForm {

    val form = Form(
        mapping(
            "email" -> email,
            "password" -> nonEmptyText,
            "rememberMe" -> boolean
        )(Data.apply)(Data.unapply)
    )

    case class Data(email: String,
                    password: String,
                    rememberMe: Boolean)


}
