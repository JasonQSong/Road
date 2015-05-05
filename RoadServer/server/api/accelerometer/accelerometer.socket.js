/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var Accelerometer = require('./accelerometer.model');

exports.register = function(socket) {
  Accelerometer.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  Accelerometer.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('accelerometer:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('accelerometer:remove', doc);
}