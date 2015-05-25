/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var CombinedHole = require('./combinedHole.model');

exports.register = function(socket) {
  CombinedHole.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  CombinedHole.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('combinedHole:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('combinedHole:remove', doc);
}