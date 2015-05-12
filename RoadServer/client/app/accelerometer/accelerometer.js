'use strict';

angular.module('roadServerApp')
  .config(function ($routeProvider) {
    $routeProvider
      .when('/accelerometer', {
        templateUrl: 'app/accelerometer/accelerometer.html',
        controller: 'AccelerometerCtrl'
      });
  });
