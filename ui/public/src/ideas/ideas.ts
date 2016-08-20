module app.ideas {

    import IHttpPromiseCallbackArg = angular.IHttpPromiseCallbackArg;
    import IHttpPromise = angular.IHttpPromise;
    import IHttpService = angular.IHttpService;
    import IStateService = angular.ui.IStateService;



    export interface IIdea {
        id: number
        description: string
        creator: IUser
        createdAt: Date
        votes: number
    }
    class Idea implements IIdea {
        id: number;
        description: string;
        creator: IUser;
        createdAt: Date;
        votes: number;
    }


    class IdeasCtrl {
        ideas: IIdea[];
        constructor(ideas: IHttpPromiseCallbackArg<IIdea[]>) {
            this.ideas = ideas.data;
        }
    }


    class NewIdeaCtrl {
        idea: IIdea =  new Idea();
        constructor(private ideaService: IIdeasService,
                    private $state: IStateService) {}

        save(idea: IIdea) {
            this.ideaService.create(idea).then(() => {
                this.$state.go('app.index', {}, { reload: true });
            });
        }
    }


    export interface IIdeasService {
        all(): IHttpPromise<IIdea[]>
        create(idea: IIdea): IHttpPromise<IIdea>
    }

    class IdeasService implements IIdeasService {

        constructor(private $http: IHttpService) {}

        all(): IHttpPromise<IIdea[]> {
            return this.$http.get('http://localhost:9000/api/ideas');
        }

        create(idea: IIdea): IHttpPromise<IIdea> {
            return this.$http.post('http://localhost:9000/api/ideas', idea);
        }

    }

    let ideasModule = angular.module('app.ideas', []);
    ideasModule.controller('IdeasCtrl', ['ideas', IdeasCtrl]);
    ideasModule.controller('NewIdeaCtrl', ['IdeasService', '$state', NewIdeaCtrl]);
    ideasModule.service('IdeasService', ['$http', IdeasService])

}
