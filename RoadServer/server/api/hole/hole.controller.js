'use strict';

var _ = require('lodash');
var fs = require('fs');
var process = require('child_process');
var Accelerometer = require('./../accelerometer/accelerometer.model');
var Hole = require('./hole.model');

// Get list of holes
exports.index = function(req, res) {
  Hole.find(function(err, holes) {
    if (err) {
      return handleError(res, err);
    }
    return res.json(200, holes);
  });
};

// Get a single hole
exports.show = function(req, res) {
  Hole.findById(req.params.id, function(err, hole) {
    if (err) {
      return handleError(res, err);
    }
    if (!hole) {
      return res.send(404);
    }
    return res.json(hole);
  });
};

// Creates a new hole in the DB.
exports.create = function(req, res) {
  Hole.create(req.body, function(err, hole) {
    if (err) {
      return handleError(res, err);
    }
    return res.json(201, hole);
  });
};

// Updates an existing hole in the DB.
exports.update = function(req, res) {
  if (req.body._id) {
    delete req.body._id;
  }
  Hole.findById(req.params.id, function(err, hole) {
    if (err) {
      return handleError(res, err);
    }
    if (!hole) {
      return res.send(404);
    }
    var updated = _.merge(hole, req.body);
    updated.save(function(err) {
      if (err) {
        return handleError(res, err);
      }
      return res.json(200, hole);
    });
  });
};

// Deletes a hole from the DB.
exports.destroy = function(req, res) {
  Hole.findById(req.params.id, function(err, hole) {
    if (err) {
      return handleError(res, err);
    }
    if (!hole) {
      return res.send(404);
    }
    hole.remove(function(err) {
      if (err) {
        return handleError(res, err);
      }
      return res.send(204);
    });
  });
};

exports.test = function(req, res) {
  Accelerometer
    .where('device', req.params.device)
    .where('timeUTC').gte(req.params.timeUTC)
    .sort('timeUTC')
    .limit(req.params.limit)
    .exec(function(err, accelerometers) {
      if (err) {
        return handleError(res, err);
      }
      var dataStr = '';
      for (var i = 0; i < accelerometers.length; i++) {
        dataStr += accelerometers[i].timeUTC + ' ';
        dataStr += accelerometers[i].x + ' ';
        dataStr += accelerometers[i].y + ' ';
        dataStr += accelerometers[i].z + '\n';
      }
      var recordFilename = '.\\data\\' + req.params.device + '-' + req.params.timeUTC + '-' + req.params.limit;
      var inputFilename = recordFilename + '.in';
      var outputFilename = recordFilename + '.out';
      fs.writeFile(inputFilename, dataStr, function(err) {
        if (err) {
          return handleError(res, err);
        }
        process.exec('..\\Pothole\\pothole.exe' + ' i ' + inputFilename + ' o ' + outputFilename + '> potholelog.txt',
          function(err) {
            if (err) {
              return handleError(res, err);
            }
            fs.readFile(outputFilename, 'utf-8', function(err, data) {
                if (err) {
                  return handleError(res, err);
                }
                if (data == '') {
                  return res.json(404, {
                    err: 'no hole'
                  })
                } else {
                  data = data.split(' ');
                  var timeStart = Number.parseInt(data[0]);
                  var timeEnd = Number.parseInt(data[1]);
                  var sumX = Number.parseFloat(data[2]);
                  var sumY = Number.parseFloat(data[3]);
                  var depth = Number.parseFloat(data[4]);
                  var velocity = Number.parseFloat(req.query.velocity);
                  var diameter = velocity * (timeEnd - timeStart);
                  console.log("params"+JSON.stringify(req.query));
                  console.log("diameter"+diameter+"v"+velocity+"timeEnd"+timeEnd+"timeStart"+timeStart);
                  var entryRatioX = 0.5;
                  var entryRatioY = 0.5;
                  if (req.params.entryRatioX !== undefined) {
                    entryRatioX = Number.parseFloat(req.query.entryRatioX);
                    entryRatioX = (entryRatioX < 0.2) ? 0.2 : entryRatioX;
                    entryRatioX = (entryRatioX > 0.8) ? 0.8 : entryRatioX;
                  }
                  if (req.params.entryRatioY !== undefined) {
                    entryRatioY = Number.parseFloat(req.query.entryRatioY);
                    entryRatioY = (entryRatioY < 0.2) ? 0.2 : entryRatioY;
                    entryRatioY = (entryRatioY > 0.8) ? 0.8 : entryRatioY;
                  }
                  depth = depth / entryRatioX / entryRatioY;
                  Hole.create({
                    device: req.params.device,
                    timeUTC: timeStart,
                    diameter: diameter,
                    depth: depth,
                    longitude:  Number.parseFloat(req.query.longitude),
                    latitude: Number.parseFloat(req.query.latitude)
                  }, function(err, hole) {
                    if (err) {
                      return handleError(res, err);
                    }
                    res.json(201, hole);
                  });
                }
              })
              //return res.json(200, accelerometers);
          })
      });
    });
};

function handleError(res, err) {
  return res.send(500, err);
}
