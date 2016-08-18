import javax.inject.Named

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{CookieSigner, Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCookieSigner, JcaCookieSignerSettings, JcaCrypter, JcaCrypterSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.daos.UserDAO
import models.daos.impl.SlickUserDAO
import models.services.impl.{IdeaServiceImpl, UserServiceImpl}
import models.services.{IdeaService, UserService}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import utils.auth.{CookieEnv, DefaultEnv}


class Module extends AbstractModule with ScalaModule {

    override def configure() = {
        bind[Clock].toInstance(Clock())
        bind[EventBus].toInstance(EventBus())
        bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
        bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
        bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
        bind[PasswordHasher].toInstance(new BCryptPasswordHasher)

        // Replace this with the bindings to your concrete DAOs
        bind[DelegableAuthInfoDAO[PasswordInfo]].toInstance(new InMemoryAuthInfoDAO[PasswordInfo])
        bind[DelegableAuthInfoDAO[OAuth1Info]].toInstance(new InMemoryAuthInfoDAO[OAuth1Info])
        bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])
        bind[DelegableAuthInfoDAO[OpenIDInfo]].toInstance(new InMemoryAuthInfoDAO[OpenIDInfo])

        bind[UserDAO].to[SlickUserDAO]

        bind[UserService].to[UserServiceImpl]
    }

    @Provides
    def provideEnvironment(userService: UserService,
                           authenticatorService: AuthenticatorService[CookieAuthenticator],
                           eventBus: EventBus): Environment[CookieEnv] = {

        Environment[CookieEnv](
            userService,
            authenticatorService,
            Seq(),
            eventBus
        )
    }

    @Provides
    def provideAuthenticatorService(@Named("authenticator-cookie-signer") cookieSigner: CookieSigner,
                                    @Named("authenticator-crypter") crypter: Crypter,
                                    fingerprintGenerator: FingerprintGenerator,
                                    idGenerator: IDGenerator,
                                    configuration: Configuration,
                                    clock: Clock): AuthenticatorService[CookieAuthenticator] = {

        val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
        val encoder = new CrypterAuthenticatorEncoder(crypter)

        new CookieAuthenticatorService(config, None, cookieSigner, encoder, fingerprintGenerator, idGenerator, clock)
    }

    @Provides @Named("authenticator-cookie-signer")
    def provideAuthenticatorCookieSigner(configuration: Configuration): CookieSigner = {
        val config = configuration.underlying.as[JcaCookieSignerSettings]("silhouette.authenticator.cookie.signer")

        new JcaCookieSigner(config)
    }

    @Provides @Named("authenticator-crypter")
    def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
        val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

        new JcaCrypter(config)
    }

    @Provides
    def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                  oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
                                  oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
                                  openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {

        new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
    }

    @Provides
    def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
        PasswordHasherRegistry(passwordHasher)
    }

}
