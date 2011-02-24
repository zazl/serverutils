/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
var fs = require('fs');
var path = require('path');
var jsp = require("uglify-js").parser;
var pro = require("uglify-js").uglify;

var providerPaths = [];

exports.addProvider = function(providerPath) {
	//console.log("provider path ["+providerPath+"] added");
	providerPaths.push(providerPath);
};

exports.addProvider(path.dirname(module.filename));

exports.readText = function(filePath, compress) {
    var contents = null;
    for (var i = 0; i < providerPaths.length; i++) {
    	contents = readTextFile(filePath, providerPaths[i]);
        if (contents !== null) {
        	if (compress) {
        		var ast = jsp.parse(contents);
        		ast = pro.ast_mangle(ast);
        		ast = pro.ast_squeeze(ast, {make_seqs: false});
        		contents = pro.gen_code(ast);
        	}
        	break;
        }
    }
    //console.log("readText : ["+filePath+"] "+ ((contents === null) ? "false" : "true"));
    return contents;
};

readTextFile = function(filePath, root) {
    filePath = path.join(root, String(filePath));
    try {
    	return fs.readFileSync(filePath, 'utf8');
    } catch(e) {
        return null;
    }
};
