'use strict';

var express = require('express');
var controller = require('./combinedHole.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/test/:lastHoleId/:timeUTC', controller.test);
router.get('/lastHoleId', controller.lastHoleId);
router.get('/getSurroundHoles', controller.getSurroundHoles);
router.get('/:id', controller.show);
router.post('/', controller.create);
router.put('/:id', controller.update);
router.patch('/:id', controller.update);
router.delete('/:id', controller.destroy);

module.exports = router;