/// <reference path="../../../typings/index.d.ts" />

var app = angular.module('ideasUiApp', [
    'app.templates',
    'ui.router'
]);


class User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;

    fullName(): string {
        return `${this.firstName} ${this.lastName}`
    }
}


class AppCtrl {

    user: User = null;
    loginUrl = 'http://localhost:9000/oauth2/access_token?grant_type=implicit&client_id=123&redirect_uri=http://localhost:3000/oauth/';
    token: string = null;

    constructor(private $log: ng.ILogService,
                private $state: angular.ui.IStateService,
                private $location: ng.ILocationService,
                private $rootScope: ng.IRootScopeService) {
        $log.info('AppCtrl started');

        this.checkToken();
        $rootScope.$on('oauthTokenUpdated', this.checkToken)
    }

    checkToken() {
        let token = localStorage.getItem('oauth.token');
        if (token) {
            this.token = token;
            this.$log.debug(`Retrieved token: ${this.token}`)
        }
    }

}

app.controller('AppCtrl', ['$log', '$state', '$location', '$rootScope', AppCtrl]);


class OAuthCtrl {
    constructor($location: ng.ILocationService, $state: angular.ui.IStateService, $scope: ng.IScope) {
        let token = $location.hash();
        localStorage.setItem('oauth.token', token);
        $scope.$emit('oauthTokenUpdated');
        $state.go('app.index')
    }
}

app.controller('OAuthCtrl', ['$location', '$state', '$scope', OAuthCtrl]);


app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', (
    $stateProvider: angular.ui.IStateProvider,
    $urlRouterProvider: angular.ui.IUrlRouterProvider,
    $locationProvider: ng.ILocationProvider) => {

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
            })

    }
]);
