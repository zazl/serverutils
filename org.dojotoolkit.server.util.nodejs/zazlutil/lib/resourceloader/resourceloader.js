/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var fs = require('fs');
var path = require('path');

var providerPaths = [];
var cache = {};

exports.addProvider = function(providerPath) {
	//console.log("provider path ["+providerPath+"] added");
	providerPaths.push(providerPath);
};

exports.addProvider(path.dirname(module.filename));

exports.readText = function(filePath) {
    filePath = String(filePath);
    if (filePath.charAt(0) === '/') {
        filePath = filePath.substring(1);
    }
    var cacheEntry = cache[filePath];
	if (cacheEntry !== undefined) {
		var ts = getTimestamp(filePath, cacheEntry.root);
		if (ts === cacheEntry.ts) {
			return cacheEntry.contents;
		}
	}

    var contents = null;
    var root = findPath(filePath);
    if (root !== null) {
    	contents = readTextFile(filePath, root);
        if (contents !== null) {
        	var ts = getTimestamp(filePath, root);
        	cache[filePath] = {contents: contents, ts: ts, root: root};
        }
    }
    //console.log("readText : ["+filePath+"] "+ ((contents === null) ? "false" : "true"));
    return contents;
};

exports.getTimestamp = function(filePath) {
	var ts = -1;

    var root = findPath(filePath);
    if (root !== null) {
    	ts = getTimestamp(filePath, root);
    }
    return ts;
};

function readTextFile(filePath, root) {
	filePath = path.join(root, String(filePath));
    if (fs.existsSync(filePath)) {
    	return fs.readFileSync(filePath, 'utf8');
    } else {
        return null;
    }
};

function getTimestamp(filePath, root) {
	filePath = path.join(root, String(filePath));
    if (fs.existsSync(filePath)) {
    	var stats = fs.statSync(filePath);
    	return stats.mtime.getTime();
    } else {
        return -1;
    }
};

function findPath(filePath) {
	var p;
    for (var i = 0; i < providerPaths.length; i++) {
    	p = path.join(providerPaths[i], String(filePath));
    	if (fs.existsSync(p)) {
    		return providerPaths[i];
    	}
    }
    return null;
};

