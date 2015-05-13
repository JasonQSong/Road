/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var Hole = require('./hole.model');

exports.register = function(socket) {
  Hole.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  Hole.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('hole:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('hole:remove', doc);
}