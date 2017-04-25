(function() {
    'use strict';

    angular
        .module('jhipsterGrpcSampleApplicationApp')
        .controller('BankAccountController', BankAccountController);

    BankAccountController.$inject = ['DataUtils', 'BankAccount'];

    function BankAccountController(DataUtils, BankAccount) {

        var vm = this;

        vm.bankAccounts = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadAll();

        function loadAll() {
            BankAccount.query(function(result) {
                vm.bankAccounts = result;
                vm.searchQuery = null;
            });
        }
    }
})();
