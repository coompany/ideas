/// <reference path="../../../typings/index.d.ts" />


module app {

    import IRootScopeService = angular.IRootScopeService;
    import ILogService = angular.ILogService;
    import IStateService = angular.ui.IStateService;
    import ILocationService = angular.ILocationService;
    import IHttpService = angular.IHttpService;
    import IHttpPromiseCallbackArg = angular.IHttpPromiseCallbackArg;
    import IStateProvider = angular.ui.IStateProvider;
    import IUrlRouterProvider = angular.ui.IUrlRouterProvider;
    import ILocationProvider = angular.ILocationProvider;
    import IHttpProvider = angular.IHttpProvider;
    import IQService = angular.IQService;
    import IWindowService = angular.IWindowService;
    import IIdeasService = app.ideas.IIdeasService;
    import IScope = angular.IScope;



    let app = angular.module('ideasUiApp', [
        'app.templates',
        'app.ideas',
        'ui.router'
    ]);


    export interface IAppRootScope extends IRootScopeService {
        loginUrl: string
        token: string
        user: IUser
    }


    export interface IUser {
        id: number
        firstName: string
        lastName: string
        email: string
    }


    class AppCtrl {

        constructor(private $log: ILogService,
                    private $state: IStateService,
                    private appScope: IAppRootScope,
                    private appConfig: IAppConfig,
                    $http: IHttpService) {

            this.appScope.loginUrl = appConfig.loginUrl;

            if ($state.current.name != 'app.oauth') {
                $http.get(`${appConfig.apiUrl}/me`).then((user: IHttpPromiseCallbackArg<IUser>) => {
                    this.appScope.user = user.data;
                });
            }
        }

    }



    export interface ITokenService {
        token: string
        checkToken(): void
    }

    class TokenService implements ITokenService {
        public token: string;

        constructor(private $http: IHttpProvider) {
            this.checkToken();
        }

        checkToken() {
            let token = localStorage.getItem('oauth.token');
            if (token) {
                this.token = token;
                this.$http.defaults.headers.common.Authorization = `Bearer ${token}`;
            } else {
                this.token = null;
            }
        }
    }



    class OAuthCtrl {
        constructor($location: ILocationService, $state: IStateService, tokenService: ITokenService) {
            let token = $location.hash().split('token=')[1];
            localStorage.setItem('oauth.token', token);
            tokenService.checkToken();
            $state.go('app.index', {}, { reload: true })
        }
    }



    export interface IAppConfig {
        clientId: string
        redirectUri: string
        loginUrl: string
        apiUrl: string
    }

    class AppConfig implements IAppConfig {
        public clientId = '123';
        public redirectUri = 'http://localhost:3000/oauth/';
        public loginUrl = `http://localhost:9000/oauth2/access_token?grant_type=implicit&client_id=${this.clientId}&redirect_uri=${this.redirectUri}`;
        public apiUrl = 'http://localhost:9000/api';
    }



    app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', (
        $stateProvider: IStateProvider,
        $urlRouterProvider: IUrlRouterProvider,
        $locationProvider: ILocationProvider,
        $httpProvider: IHttpProvider) => {

            $locationProvider.html5Mode(true);

            $urlRouterProvider.otherwise('/');

            $stateProvider
                .state('app', {
                    abstract: true,
                    controller: 'AppCtrl',
                    controllerAs: 'app',
                    templateUrl: 'app-templates/app/main.html'
                })
                .state('app.oauth', {
                    url: '/oauth/',
                    controller: 'OAuthCtrl'
                })
                .state('app.index', {
                    url: '/',
                    controller: 'IdeasCtrl',
                    controllerAs: 'ideas',
                    templateUrl: 'app-templates/ideas/index.html',
                    resolve: {
                        ideas: ['IdeasService', (ideasService: IIdeasService) => {
                            return ideasService.all();
                        }]
                    }
                })
                .state('app.new', {
                    url: '/new',
                    controller: 'NewIdeaCtrl',
                    controllerAs: 'ideasNew',
                    templateUrl: 'app-templates/ideas/new.html'
                });



            let token = localStorage.getItem('oauth.token');
            if (token) {
                $httpProvider.defaults.headers.common.Authorization = `Bearer ${token}`;
            }

            $httpProvider.interceptors.push(['$q', '$window', 'appConfig',
                ($q: IQService, $window: IWindowService, appConfig: IAppConfig) => {
                    return {
                        'responseError': <T>(response: IHttpPromiseCallbackArg<T>) => {
                            if (response.status === 401 ||
                                response.headers('WWW-Authenticate').indexOf('invalid_request') != -1) {
                                $window.location.href = appConfig.loginUrl;
                            }
                            return $q.reject(response);
                        }
                    }
                }
            ]);

        }
    ]);




    app.controller('AppCtrl', ['$log', '$state', '$rootScope', 'appConfig', '$http', AppCtrl]);
    app.controller('OAuthCtrl', ['$location', '$state', 'TokenService', OAuthCtrl]);

    app.service('TokenService', ['$http', TokenService]);

    app.constant('appConfig', new AppConfig());

}
