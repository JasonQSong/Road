'use strict';

var express = require('express');
var controller = require('./hole.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/test/:device/:timeUTC/:limit', controller.test);
router.get('/:id', controller.show);
router.post('/', controller.create);
router.put('/:id', controller.update);
router.patch('/:id', controller.update);
router.delete('/:id', controller.destroy);

module.exports = router;