'use strict';

angular.module('roadServerApp')
  .controller('AccelerometerCtrl', ['$scope','$http','socket',
  function ($scope,$http,socket) {
    $scope.message = 'Hello';
    $http.get('/api/accelerometers').success(function(data) {
      $scope.data = data;
      socket.syncUpdates('data', $scope.data);
    });
    $scope.genAccelerometer=function(){
      var accelerometer = {
        device: 1,
        longitudinal: 2,
        transverse: 3,
        time: 4,
        longitude: 5,
        latitude: 6,
        x: 7,
        y: 8,
        z: 9,
      };
      $http.post('/api/accelerometers', accelerometer);
    }
  }]);
