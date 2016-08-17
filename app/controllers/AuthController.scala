package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.actions.UserAwareRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Controller, Result}
import models.services.UserService
import utils.auth.DefaultEnv

import scala.concurrent.Future


class AuthController @Inject() (
    silhouette: Silhouette[DefaultEnv],
    val messagesApi: MessagesApi,
    userService: UserService,
    passwordHasher: PasswordHasher,
    authInfoRepository: AuthInfoRepository) extends Controller with I18nSupport {

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
                                loginInfo = loginInfo,
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
