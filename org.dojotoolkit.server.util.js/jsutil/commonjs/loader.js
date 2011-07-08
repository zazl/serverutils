/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/

var root = root || "/";
var config = config || {};

(function(global, root, config) {
	var modules = global.modules = global.modules || {};
	var stack = [];
	var aliases = config.paths || {};
	var opts = Object.prototype.toString;
	
    function isFunction(it) { return opts.call(it) === "[object Function]"; };
    function isArray(it) { return opts.call(it) === "[object Array]"; };
    function isString(it) { return (typeof it == "string" || it instanceof String); };
    
	function normalize(path) {
		var segments = path.split('/');
		var skip = 0;

		for (var i = segments.length; i >= 0; i--) {
			var segment = segments[i];
			if (segment === '.') {
				segments.splice(i, 1);
			} else if (segment === '..') {
				segments.splice(i, 1);
				skip++;
			} else if (skip) {
				segments.splice(i, 1);
				skip--;
			}
		}
		return segments.join('/');
	};
	
	function expand(id) {
		var parent = stack.length > 0 ? stack[stack.length-1].module : {id: "top", path: root};
		var path;
		
		var isRelative = id.search(/^\.\/|^\.\.\//) === -1 ? false : true;
		if (isRelative) {
			var parentPath = parent.path.substring(0, parent.path.lastIndexOf('/')+1);
			path = parentPath + id;
		} else {
			if (id.charAt(0) === '/') {
				path = id.substring(1);
			} else {
				path = id;
			}
		}		
		path = normalize(path);
		
		if (aliases[path]) {
			path = aliases[path];
		}
		
		return path;
	};
	
	var require = global.require = function(id) {
		if (id.match(".+!.+")) {
			var pluginName = id.substring(0, id.indexOf('!'));
			pluginName = expand(pluginName);
			var pluginModuleName = id.substring(id.indexOf('!')+1);
			var plugin = require(pluginName);
			if (plugin.normalize) {
				pluginModuleName = plugin.normalize(pluginModuleName, expand); 
			} else {
				pluginModuleName = expand(pluginModuleName);
			}
		    if (modules[pluginName+"!"+pluginModuleName]) {
		        return modules[pluginName+"!"+pluginModuleName].exports;
		    }
			if (plugin.load) {
		    	modules[pluginName+"!"+pluginModuleName] = {module: {id:id, path: pluginName+"!"+pluginModuleName}, exports: {}};
				var req = require;
				req.nameToUrl = function(moduleResource, ext) {
					return expand(moduleResource+"."+ext);
				};
				req.toUrl = function(moduleResource) {
					return expand(moduleResource);
				};
				req.isDefined = function(moduleName) {
					return expand(moduleName) in modules;
				};
				plugin.load(pluginModuleName, req, function(value){
			    	modules[pluginName+"!"+pluginModuleName].exports = value;
				}, config);
				return modules[pluginName+"!"+pluginModuleName].exports;
			} else {
				throw new Error("Plugin ["+pluginName+"] does not contain a load function");
			}
		} else {
			var path = expand(id);
			
		    if (modules[path]) {
		        return modules[path].exports;
		    }
		    
	    	var exports = {};
	    	var currentModule = {id:id, path: path};
	    	modules[path] = {module: currentModule, exports: exports};
	    	stack.push(modules[path]);
	    	if (loadCommonJSModule(root+path+".js", modules[path]) === null) {
	    		throw new Error("Unable to load ["+path+"]");
	    	}
	    	stack.pop();
	    	if (modules[path].factory !== undefined) {
				exports = modules[path].exports = modules[path].factory.apply(null, modules[path].dependencyArgs);
	    	} else if (modules[path].objectLiteral !== undefined) {
	    		exports = modules[path].exports = modules[path].objectLiteral;
	    	}
			return exports;
		}
	};
	
	var define = global.define = function (id, dependencies, factory) {
		if (!isString(id)) {
			factory = dependencies;
			dependencies = id;
			id = null;
		}
		if (!isArray(dependencies)) {
			factory = dependencies;
			dependencies = [];
		}
		var module = stack[stack.length-1];
		if (isFunction(factory)) { 
			module.factory = factory;
			var dependencyArgs = [];
	
			for (var i = 0; i < dependencies.length; i++) {
				if (dependencies[i] === 'require') {
					dependencyArgs[i] = require;
				} else if (dependencies[i] === 'module') {
					dependencyArgs[i] = module;
				} else if (dependencies[i] === 'exports') {
					dependencyArgs[i] = module.exports;
				} else {
					dependencyArgs[i] = require(dependencies[i]);
				}
			}
			module.dependencyArgs = dependencyArgs;
		} else {
			module.objectLiteral = factory;
		}
	};
})(this, root, config);