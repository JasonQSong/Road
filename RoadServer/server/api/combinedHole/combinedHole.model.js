'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var CombinedHoleSchema = new Schema({
  diameter: Number,
  depth: Number,  
  longitude: Number,
  latitude: Number,
  trust:Number,
  lastHole_id:String,
});

module.exports = mongoose.model('CombinedHole', CombinedHoleSchema);