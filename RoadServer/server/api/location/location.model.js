'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var LocationSchema = new Schema({
  device: String,
  timeUTC: Number,
  latitude: Number,
  longitude: Number,
  direction:Number,
  velocity:Number,
  locType:Number,
});

module.exports = mongoose.model('Location', LocationSchema);