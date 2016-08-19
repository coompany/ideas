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

    public user: User = null;
    private token: string = null;

    constructor(private $log: ng.ILogService,
                private $state: angular.ui.IStateService,
                private $location: ng.ILocationService) {
        this.$log.info('AppCtrl started');

        let token = localStorage.getItem('oauth.token');
        if (token) {
            this.token = token;
            $log.debug(`Retrieved token: ${this.token}`)
        }

    }

}

app.controller('AppCtrl', ['$log', '$state', '$location', AppCtrl]);


class OAuthCtrl {
    constructor($location: ng.ILocationService, $state: angular.ui.IStateService) {
        let token = $location.hash();
        localStorage.setItem('oauth.token', token);
        $state.go('app.index')
    }
}

app.controller('OAuthCtrl', ['$location', '$state', OAuthCtrl]);


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
