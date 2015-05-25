'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var HoleSchema = new Schema({
  device: String,
  timeUTC:Number,
  diameter: Number,
  depth: Number,  
  longitude: Number,
  latitude: Number,
});

module.exports = mongoose.model('Hole', HoleSchema);