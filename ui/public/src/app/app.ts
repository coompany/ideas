/// <reference path="../../../typings/index.d.ts" />

var app = angular.module('ideasUiApp', [
    'app.templates',
    'ui.router'
]);


interface IAppRootScope extends ng.IRootScopeService {
    loginUrl: string
    token: string
    user: IUser
}


interface IUser {
    id: number
    firstName: string
    lastName: string
    email: string
}


class AppCtrl {

    constructor(private $log: ng.ILogService,
                private $state: angular.ui.IStateService,
                private $location: ng.ILocationService,
                private appScope: IAppRootScope,
                private $http: ng.IHttpService) {

        this.appScope.loginUrl = 'http://localhost:9000/oauth2/access_token?grant_type=implicit&client_id=123&redirect_uri=http://localhost:3000/oauth/';

        if ($state.current.name != 'app.oauth') {
            this.checkToken();
        }
    }

    checkToken() {
        let token = localStorage.getItem('oauth.token');
        if (token) {
            this.appScope.token = token;
            this.$log.debug(`Retrieved token: ${token}`);
            this.$http.defaults.headers.common.Authorization = `Bearer ${token}`;
            this.$http.get('http://localhost:9000/api/me').then((user: ng.IHttpPromiseCallbackArg<IUser>) => {
                this.appScope.user = user.data;
            })
        }
    }

}

app.controller('AppCtrl', ['$log', '$state', '$location', '$rootScope', '$http', AppCtrl]);


class OAuthCtrl {
    constructor($location: ng.ILocationService, $state: angular.ui.IStateService) {
        let token = $location.hash().split('token=')[1];
        localStorage.setItem('oauth.token', token);
        $state.go('app.index', {}, { reload: true })
    }
}

app.controller('OAuthCtrl', ['$location', '$state', OAuthCtrl]);


app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', (
    $stateProvider: angular.ui.IStateProvider,
    $urlRouterProvider: angular.ui.IUrlRouterProvider,
    $locationProvider: ng.ILocationProvider,
    $httpProvider: ng.IHttpProvider) => {

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
                templateUrl: 'app-templates/index/index.html'
            });


        $httpProvider.interceptors.push(['$q', '$window', '$rootScope',
            ($q: ng.IQService, $window: ng.IWindowService, appScope: IAppRootScope) => {
                return {
                    'responseError': <T>(response: ng.IHttpPromiseCallbackArg<T>) => {
                        if (response.status === 401) {
                            $window.location.href = appScope.loginUrl;
                        }
                        return $q.reject(response);
                    }
                }
        }]);

    }
]);
