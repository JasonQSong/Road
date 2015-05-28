'use strict';

var _ = require('lodash');
var fs = require('fs');
var process = require('child_process');
var CombinedHole = require('./combinedHole.model');
var Accelerometer = require('./../accelerometer/accelerometer.model');
var Hole = require('./../hole/hole.model');

// Get list of combinedHoles
exports.index = function(req, res) {
  CombinedHole.find(function(err, combinedHoles) {
    if (err) {
      return handleError(res, err);
    }
    return res.json(200, combinedHoles);
  });
};

// Get a single combinedHole
exports.show = function(req, res) {
  CombinedHole.findById(req.params.id, function(err, combinedHole) {
    if (err) {
      return handleError(res, err);
    }
    if (!combinedHole) {
      return res.send(404);
    }
    return res.json(combinedHole);
  });
};

// Creates a new combinedHole in the DB.
exports.create = function(req, res) {
  CombinedHole.create(req.body, function(err, combinedHole) {
    if (err) {
      return handleError(res, err);
    }
    return res.json(201, combinedHole);
  });
};

// Updates an existing combinedHole in the DB.
exports.update = function(req, res) {
  if (req.body._id) {
    delete req.body._id;
  }
  CombinedHole.findById(req.params.id, function(err, combinedHole) {
    if (err) {
      return handleError(res, err);
    }
    if (!combinedHole) {
      return res.send(404);
    }
    var updated = _.merge(combinedHole, req.body);
    updated.save(function(err) {
      if (err) {
        return handleError(res, err);
      }
      return res.json(200, combinedHole);
    });
  });
};

// Deletes a combinedHole from the DB.
exports.destroy = function(req, res) {
  CombinedHole.findById(req.params.id, function(err, combinedHole) {
    if (err) {
      return handleError(res, err);
    }
    if (!combinedHole) {
      return res.send(404);
    }
    combinedHole.remove(function(err) {
      if (err) {
        return handleError(res, err);
      }
      return res.send(204);
    });
  });
};

function handleError(res, err) {
  return res.send(500, err);
}

exports.lastHoleId = function(req, res) {
  CombinedHole
    .findOne()
    .sort('-lastHole_id')
    .exec(function(err, combinedHole) {
      if (err) {
        return handleError(res, err);
      }
      if (!combinedHole) {
        return res.send(404);
      }
      return res.send(combinedHole.lastHole_id);
    });

}
exports.getSurroundHoles = function(req, res) {
  CombinedHole
    .findOne()
    .sort('-lastHole_id')
    .exec(function(err, combinedHole) {
      if (err) {
        return handleError(res, err);
      }
      if (!combinedHole) {
        return res.send(404);
      }
      var inputLastHoleId = '0';
      if (combinedHole !== null) {
        inputLastHoleId = combinedHole.lastHole_id;
      }
      var longitudeMin = Number.parseFloat(req.query.longitude?req.query.longitude:0) - 0.01;
      var longitudeMax = Number.parseFloat(req.query.longitude?req.query.longitude:0) + 0.01;
      var latitudeMin = Number.parseFloat(req.query.latitude?req.query.latitude:0) - 0.01;
      var latitudeMax = Number.parseFloat(req.query.latitude?req.query.latitude:0) + 0.01;

      CombinedHole
        .where('lastHole_id').gte(inputLastHoleId)
        .where('longitude').gte(longitudeMin)
        .where('longitude').lte(longitudeMax)
        .where('latitude').gte(latitudeMin)
        .where('latitude').lte(latitudeMax)
        .exec(function(err, combinedHoles) {
          if (err) {
            return handleError(res, err);
          }
          return res.json(200, combinedHoles);
        });
    });

}


exports.test = function(req, res) {
  Hole
    .find(function(err, holes) {
      if (err) {
        return handleError(res, err);
      }
      var dataHoleStr = '';
      for (var i = 0; i < holes.length; i++) {
        dataHoleStr += holes[i]._id + ' ';
        dataHoleStr += (holes[i].diameter ? holes[i].diameter : 0) + ' ';
        dataHoleStr += (holes[i].depth ? holes[i].depth : 0) + ' ';
        dataHoleStr += (holes[i].longitude ? holes[i].longitude : 0) + ' ';
        dataHoleStr += (holes[i].latitude ? holes[i].latitude : 0) + '\n';
      }
      CombinedHole
        .findOne()
        .sort('-lastHole_id')
        .exec(function(err, combinedHole) {
          if (err) {
            return handleError(res, err);
          }
          var inputLastHoleId = '0';
          if (combinedHole !== null) {
            inputLastHoleId = combinedHole.lastHole_id;
            console.log('combinedHole' + combinedHole.lastHole_id);
          }
          if (inputLastHoleId >= req.params.lastHoleId) {
            return res.json(200, {});
          }
          CombinedHole
            .where('lastHole_id').gte(inputLastHoleId)
            .exec(function(err, combinedHoles) {
              if (err) {
                return handleError(res, err);
              }
              var dataMeanStr = '';
              for (var i = 0; i < combinedHoles.length; i++) {
                dataMeanStr += (combinedHoles[i].diameter ? combinedHoles[i].diameter : 0) + ' ';
                dataMeanStr += (combinedHoles[i].depth ? combinedHoles[i].depth : 0) + ' ';
                dataMeanStr += (combinedHoles[i].longitude ? combinedHoles[i].longitude : 0) + ' ';
                dataMeanStr += (combinedHoles[i].latitude ? combinedHoles[i].latitude : 0) + ' ';
                dataMeanStr += (combinedHoles[i].trust ? combinedHoles[i].trust : 0) + '\n';
              }
              var dataDir = '.\\data_kmean\\'
              var inputHoleFilename = dataDir + req.params.lastHoleId + '-' + (new Date()).getTime() + '-hole.in'
              var inputMeanFilename = dataDir + inputLastHoleId + '-' + (new Date()).getTime() + '-mean.in';
              var outputMeanFilename = dataDir + req.params.lastHoleId + '-' + (new Date()).getTime() + '-mean.out';
              var logFilename = dataDir + req.params.lastHoleId + '-' + (new Date()).getTime() + '.log';
              fs.writeFile(inputHoleFilename, dataHoleStr, function(err) {
                if (err) {
                  return handleError(res, err);
                }
                fs.writeFile(inputMeanFilename, dataMeanStr, function(err) {
                  if (err) {
                    return handleError(res, err);
                  }
                  var cmd = '..\\StepKMean\\StepKMean.exe' + ' iid ' + inputLastHoleId + ' ih ' + inputHoleFilename + ' im ' + inputMeanFilename + ' oid ' + req.params.lastHoleId + ' om ' + outputMeanFilename + ' > ' + logFilename;
                  process.exec(cmd,
                    function(err) {
                      if (err) {
                        return handleError(res, err);
                      }
                      fs.readFile(outputMeanFilename, 'utf-8', function(err, data) {
                        if (err) {
                          return handleError(res, err);
                        }
                        if (data == '') {
                          return res.json(404, {
                            err: 'no hole'
                          });
                        } else {
                          var errs = [];
                          var combinedHoles = [];
                          var lines = data.split('\n');
                          for (var i in lines) {
                            if (lines[i] == '') {
                              continue;
                            }
                            var fields = lines[i].split(' ');
                            var diameter = Number.parseFloat(fields[0]);
                            var depth = Number.parseFloat(fields[1]);
                            var longitude = Number.parseFloat(fields[2]);
                            var latitude = Number.parseFloat(fields[3]);
                            var trust = Number.parseInt(fields[4]);
                            CombinedHole.create({
                              diameter: diameter,
                              depth: depth,
                              longitude: longitude,
                              latitude: latitude,
                              trust: trust,
                              lastHole_id: req.params.lastHoleId,
                            }, function(err, combinedHole) {
                              if (err) {
                                errs.push(err);
                              } else {
                                combinedHoles.push(combinedHole);
                              }
                            });
                          }
                          if (errs.length > 0) {
                            return handleError(res, errs);
                          } else {
                            return res.json(201, combinedHoles);
                          }
                        }
                      });
                    });
                });
              });
            });
        });
    });
};
