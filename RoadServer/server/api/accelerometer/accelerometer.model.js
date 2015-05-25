'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var AccelerometerSchema = new Schema({
  device: String,
  timeUTC: Number,
  x: Number,
  y: Number,
  z: Number
});

module.exports = mongoose.model('Accelerometer', AccelerometerSchema);

//db.accelerometers.ensureIndex({unique:1},{unique:true}) 