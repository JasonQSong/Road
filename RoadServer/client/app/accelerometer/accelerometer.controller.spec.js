'use strict';

describe('Controller: AccelerometerCtrl', function () {

  // load the controller's module
  beforeEach(module('roadServerApp'));

  var AccelerometerCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AccelerometerCtrl = $controller('AccelerometerCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
