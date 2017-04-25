(function() {
    'use strict';
    angular
        .module('jhipsterGrpcSampleApplicationApp')
        .factory('BankAccount', BankAccount);

    BankAccount.$inject = ['$resource', 'DateUtils'];

    function BankAccount ($resource, DateUtils) {
        var resourceUrl =  'api/bank-accounts/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.openingDay = DateUtils.convertLocalDateFromServer(data.openingDay);
                        data.lastOperationDate = DateUtils.convertDateTimeFromServer(data.lastOperationDate);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.openingDay = DateUtils.convertLocalDateToServer(copy.openingDay);
                    return angular.toJson(copy);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    var copy = angular.copy(data);
                    copy.openingDay = DateUtils.convertLocalDateToServer(copy.openingDay);
                    return angular.toJson(copy);
                }
            }
        });
    }
})();
