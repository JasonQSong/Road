'use strict';

/**
 * @ngdoc function
 * @name roadServerApp.controller:AboutCtrl
 * @description
 * # AboutCtrl
 * Controller of the roadServerApp
 */
angular.module('roadServerApp')
  .controller('AboutCtrl', function ($scope) {
    $scope.awesomeThings = [
      'HTML5 Boilerplate',
      'AngularJS',
      'Karma'
    ];
  });
