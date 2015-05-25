'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var EntrySchema = new Schema({
  device: String,
  timeUTC: Number,
  transverse: Number,
  longitudinal: Number,
});

module.exports = mongoose.model('Entry', EntrySchema);