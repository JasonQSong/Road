'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var AccelerometerSchema = new Schema({
  device: Number,
  longitudinal: Number,
  transverse: Number,
  time: Number,
  longitude: Number,
  latitude: Number,
  x: Number,
  y: Number,
  z: Number
});

module.exports = mongoose.model('Accelerometer', AccelerometerSchema);