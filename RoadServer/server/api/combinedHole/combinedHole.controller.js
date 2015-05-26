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

      CombinedHole.find(function(err, combinedHoles) {
        if (err) {
          return handleError(res, err);
        }
        var dataMeanStr = '';
        var inputLastHoleId = '0';
        for (var i = 0; i < combinedHoles.length; i++) {
          dataMeanStr += (combinedHoles[i].diameter ? combinedHoles[i].diameter : 0) + ' ';
          dataMeanStr += (combinedHoles[i].diameter ? combinedHoles[i].depth : 0) + ' ';
          dataMeanStr += (combinedHoles[i].diameter ? combinedHoles[i].longitude : 0) + ' ';
          dataMeanStr += (combinedHoles[i].diameter ? combinedHoles[i].latitude : 0) + ' ';
          dataMeanStr += (combinedHoles[i].trust ? combinedHoles[i].trust : 0) + '\n';
          inputLastHoleId = (combinedHoles[i].lastHole_id > inputLastHoleId ? combinedHoles[i].lastHole_id : inputLastHoleId) ;
        }

        var dataDir = '.\\data_kmean\\'
        var inputHoleFilename = dataDir + req.params.lastHoleId + '-' + (new Date()).getTime() + '-hole.in'
        var inputMeanFilename = inputLastHoleId + '-' + (new Date()).getTime() + '-mean.in';
        var outputMeanFilename = req.params.lastHoleId + '-' + (new Date()).getTime() + '-mean.out';
        fs.writeFile(inputHoleFilename, dataHoleStr, function(err) {
          if (err) {
            return handleError(res, err);
          }
          fs.writeFile(inputMeanFilename, dataMeanStr, function(err) {
            if (err) {
              return handleError(res, err);
            }
          });
          res.json(200, {});
          // process.exec('..\\Pothole\\pothole.exe' + ' i ' + inputFilename + ' o ' + outputFilename + '> potholelog.txt',
          //   function(err) {
          //     if (err) {
          //       return handleError(res, err);
          //     }
          //     fs.readFile(outputFilename, 'utf-8', function(err, data) {
          //         if (err) {
          //           return handleError(res, err);
          //         }
          //         if (data == '') {
          //           return res.json(404, {
          //             err: 'no hole'
          //           })
          //         } else {
          //           data = data.split(' ');
          //           var timeStart = Number.parseInt(data[0]);
          //           var timeEnd = Number.parseInt(data[1]);
          //           var sumX = Number.parseFloat(data[2]);
          //           var sumY = Number.parseFloat(data[3]);
          //           var depth = Number.parseFloat(data[4]);
          //           var velocity = Number.parseFloat(req.query.velocity);
          //           var diameter = velocity * (timeEnd - timeStart);
          //           console.log("params"+JSON.stringify(req.query));
          //           console.log("diameter"+diameter+"v"+velocity+"timeEnd"+timeEnd+"timeStart"+timeStart);
          //           var entryRatioX = 0.5;
          //           var entryRatioY = 0.5;
          //           if (req.params.entryRatioX !== undefined) {
          //             entryRatioX = Number.parseFloat(req.query.entryRatioX);
          //             entryRatioX = (entryRatioX < 0.2) ? 0.2 : entryRatioX;
          //             entryRatioX = (entryRatioX > 0.8) ? 0.8 : entryRatioX;
          //           }
          //           if (req.params.entryRatioY !== undefined) {
          //             entryRatioY = Number.parseFloat(req.query.entryRatioY);
          //             entryRatioY = (entryRatioY < 0.2) ? 0.2 : entryRatioY;
          //             entryRatioY = (entryRatioY > 0.8) ? 0.8 : entryRatioY;
          //           }
          //           depth = depth / entryRatioX / entryRatioY;
          //           Hole.create({
          //             device: req.params.device,
          //             timeUTC: timeStart,
          //             diameter: diameter,
          //             depth: depth,
          //             longitude:  Number.parseFloat(req.query.longitude),
          //             latitude: Number.parseFloat(req.query.latitude)
          //           }, function(err, hole) {
          //             if (err) {
          //               return handleError(res, err);
          //             }
          //             res.json(201, hole);
          //           });
          //         }
          //       })
          //       //return res.json(200, accelerometers);
          //   })
        });
        return res.json(200, combinedHoles);
      });
    });
};
