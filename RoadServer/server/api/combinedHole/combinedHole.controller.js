'use strict';

var _ = require('lodash');
var CombinedHole = require('./combinedHole.model');

// Get list of combinedHoles
exports.index = function(req, res) {
  CombinedHole.find(function (err, combinedHoles) {
    if(err) { return handleError(res, err); }
    return res.json(200, combinedHoles);
  });
};

// Get a single combinedHole
exports.show = function(req, res) {
  CombinedHole.findById(req.params.id, function (err, combinedHole) {
    if(err) { return handleError(res, err); }
    if(!combinedHole) { return res.send(404); }
    return res.json(combinedHole);
  });
};

// Creates a new combinedHole in the DB.
exports.create = function(req, res) {
  CombinedHole.create(req.body, function(err, combinedHole) {
    if(err) { return handleError(res, err); }
    return res.json(201, combinedHole);
  });
};

// Updates an existing combinedHole in the DB.
exports.update = function(req, res) {
  if(req.body._id) { delete req.body._id; }
  CombinedHole.findById(req.params.id, function (err, combinedHole) {
    if (err) { return handleError(res, err); }
    if(!combinedHole) { return res.send(404); }
    var updated = _.merge(combinedHole, req.body);
    updated.save(function (err) {
      if (err) { return handleError(res, err); }
      return res.json(200, combinedHole);
    });
  });
};

// Deletes a combinedHole from the DB.
exports.destroy = function(req, res) {
  CombinedHole.findById(req.params.id, function (err, combinedHole) {
    if(err) { return handleError(res, err); }
    if(!combinedHole) { return res.send(404); }
    combinedHole.remove(function(err) {
      if(err) { return handleError(res, err); }
      return res.send(204);
    });
  });
};

function handleError(res, err) {
  return res.send(500, err);
}