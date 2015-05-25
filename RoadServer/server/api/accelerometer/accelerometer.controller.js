'use strict';

var _ = require('lodash');
var Accelerometer = require('./accelerometer.model');

// Get list of accelerometers
exports.index = function(req, res) {
  Accelerometer.find(function (err, accelerometers) {
    if(err) { return handleError(res, err); }
    return res.json(200, accelerometers);
  });
};

// Get a single accelerometer
exports.show = function(req, res) {
  Accelerometer.findById(req.params.id, function (err, accelerometer) {
    if(err) { return handleError(res, err); }
    if(!accelerometer) { return res.send(404); }
    return res.json(accelerometer);
  });
};

// Creates a new accelerometer in the DB.
exports.create = function(req, res) {
  Accelerometer.create(req.body, function(err, accelerometer) {
    console.log("acc create:"+JSON.stringify(req.body));
    if(err) { return handleError(res, err); }
    return res.json(201, accelerometer);
  });
};

exports.createArray = function(req, res) {
  var errs=[];
  var acceleros=[];
  for(var i=0;i<req.body.length;i++){
    Accelerometer.create(req.body[i], function(err, accelerometer) {
      if(err) {errs.push(err); }
      else{ acceleros.push(accelerometer); }
    });
  }
  if(errs.length>0){
    return handleError(res,errs);
  }else{
    res.json(201,acceleros);
  }
};

// Updates an existing accelerometer in the DB.
exports.update = function(req, res) {
  if(req.body._id) { delete req.body._id; }
  Accelerometer.findById(req.params.id, function (err, accelerometer) {
    if (err) { return handleError(res, err); }
    if(!accelerometer) { return res.send(404); }
    var updated = _.merge(accelerometer, req.body);
    updated.save(function (err) {
      if (err) { return handleError(res, err); }
      return res.json(200, accelerometer);
    });
  });
};

// Deletes a accelerometer from the DB.
exports.destroy = function(req, res) {
  Accelerometer.findById(req.params.id, function (err, accelerometer) {
    if(err) { return handleError(res, err); }
    if(!accelerometer) { return res.send(404); }
    accelerometer.remove(function(err) {
      if(err) { return handleError(res, err); }
      return res.send(204);
    });
  });
};

function handleError(res, err) {
  console.log("err:"+err)
  return res.send(500, err);
}