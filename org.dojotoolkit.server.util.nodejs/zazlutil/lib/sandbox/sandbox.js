/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var path = require('path');
var vm = require('vm');
var http = require('http');
var url = require('url');
var resourceloader = require('zazlutil').resourceloader;
resourceloader.addProvider(path.dirname(module.filename));

var scripts = {};

Sandbox = function(global) {
	global.sandbox = this;
	global.console = console;
	global.print = console;
	global.setTimeout = setTimeout;
	global.setInterval = setInterval;
	global.clearTimeout = clearTimeout;
	global.clearInterval = clearInterval;
	global.http = http;
	global.url = url;
	this.ctx = vm.createContext(global);
};

Sandbox.prototype = {
	loadJS: function(moduleName) {
	    if (moduleName.charAt(0) == '/') {
	        moduleName = moduleName.substring(1);
	    }
	    moduleName = path.normalize(moduleName);
		var script = scripts[moduleName];
		if (script === undefined) {
		    var moduleContents = resourceloader.readText(moduleName);
		    if (moduleContents !== null) {
				script = vm.createScript(moduleContents, moduleName);
				scripts[moduleName] = script;
		    }
		}
	    var result = null;
	    if (script !== undefined) {
	        //console.log("script : ["+moduleName+"]");
	    	result = script.runInContext(this.ctx);
	    }
	    return result;
	}	
};

exports.createSandbox = function(sandbox) {
	return new Sandbox(sandbox);
};
