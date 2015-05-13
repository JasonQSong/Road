'use strict';

var _ = require('lodash');
var fs = require('fs');
var process = require('child_process');
var Accelerometer = require('./../accelerometer/accelerometer.model');
var Hole = require('./hole.model');

// Get list of holes
exports.index = function(req, res) {
  Hole.find(function (err, holes) {
    if(err) { return handleError(res, err); }
    return res.json(200, holes);
  });
};

// Get a single hole
exports.show = function(req, res) {
  Hole.findById(req.params.id, function (err, hole) {
    if(err) { return handleError(res, err); }
    if(!hole) { return res.send(404); }
    return res.json(hole);
  });
};

// Creates a new hole in the DB.
exports.create = function(req, res) {
  Hole.create(req.body, function(err, hole) {
    if(err) { return handleError(res, err); }
    return res.json(201, hole);
  });
};

// Updates an existing hole in the DB.
exports.update = function(req, res) {
  if(req.body._id) { delete req.body._id; }
  Hole.findById(req.params.id, function (err, hole) {
    if (err) { return handleError(res, err); }
    if(!hole) { return res.send(404); }
    var updated = _.merge(hole, req.body);
    updated.save(function (err) {
      if (err) { return handleError(res, err); }
      return res.json(200, hole);
    });
  });
};

// Deletes a hole from the DB.
exports.destroy = function(req, res) {
  Hole.findById(req.params.id, function (err, hole) {
    if(err) { return handleError(res, err); }
    if(!hole) { return res.send(404); }
    hole.remove(function(err) {
      if(err) { return handleError(res, err); }
      return res.send(204);
    });
  });
};

exports.test=function(req, res) {
  Accelerometer
    .where('device',req.params.device)
    .where('time').gte(req.params.time)
    .sort('time')
    .limit(req.params.limit)
    .exec(function (err, accelerometers) {
      if(err) { return handleError(res, err); }
      var dataStr='';
      for(var i=0;i<accelerometers.length;i++){
        dataStr+=accelerometers[i].time+' ';
        dataStr+=accelerometers[i].x+' ';
        dataStr+=accelerometers[i].y+' ';
        dataStr+=accelerometers[i].z+'\n';
      }
      var recordFilename='.\\data\\'+req.params.device+'-'+req.params.time+'-'+req.params.limit;
      var inputFilename=recordFilename+'.in';
      var outputFilename=recordFilename+'.out';
      //writeFile(inputFilename);
      fs.writeFile(inputFilename, dataStr, function(err){  
        if(err) { return handleError(res, err); }

        inputFilename='.\\..\\Wenzhuo\\data\\2015-1-22\\1.txt';
        process.exec('..\\Pothole\\pothole.exe'+' i '+inputFilename+' o ' +outputFilename,
          function(err){
            if(err) { return handleError(res, err); }
            fs.readFile(outputFilename,'utf-8',function(err,data){
              if(err) { return handleError(res, err); }
              if(data==''){
                return res.json(404,{err:'no hole'})
              }else{
                data=data.split(' ');
                var diameter=Number.parseFloat(data[0]);
                var depth=Number.parseFloat(data[1]);
                Hole.create({diameter:diameter,depth:depth}, function(err, hole) {
                  if(err) { return handleError(res, err); }
                  return res.json(201, hole);
                });
              }
            })
            //return res.json(200, accelerometers);
        })
        //TODO execute matlab
    /*
  Hole.create(req.body, function(err, hole) {
    if(err) { return handleError(res, err); }
    return res.json(201, hole);
  });
*/
      }); 
    });
};

function handleError(res, err) {
  return res.send(500, err);
}