/*
    Copyright (c) 2004-2012, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/

exports.getAst = function(uri, astparser) {
	try {
		var ast = getAst(uri, function() {
			var resourceloader = require('zazlutil').resourceloader;
			var src = resourceloader.readText(uri);
			if (src === null) {
				throw new Error("Unable to load src for ["+uri+"]");
			}
			var ast = null;
			if (astparser === "uglifyjs") {
				var jsp = require("uglify-js").parser;
				ast = jsp.parse(src, false, true);
			} else if (astparser === "esprima") {
				var esprima = require("esprima/esprima");
				ast = esprima.parse(src, {range: true});
			}
			return ast;
		});
		return ast;
	} catch (e) {
		return null;
	}
}
