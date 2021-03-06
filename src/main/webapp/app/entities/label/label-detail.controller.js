(function() {
    'use strict';

    angular
        .module('jhipsterGrpcSampleApplicationApp')
        .controller('LabelDetailController', LabelDetailController);

    LabelDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Label', 'Operation'];

    function LabelDetailController($scope, $rootScope, $stateParams, previousState, entity, Label, Operation) {
        var vm = this;

        vm.label = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('jhipsterGrpcSampleApplicationApp:labelUpdate', function(event, result) {
            vm.label = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
