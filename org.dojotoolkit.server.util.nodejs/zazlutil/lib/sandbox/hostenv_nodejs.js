/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
if(dojo.config["baseUrl"]){
    dojo.baseUrl = dojo.config["baseUrl"];
}else{
    dojo.baseUrl = "/";
}

dojo._name = 'nodejs';
dojo.isRhino = false;

if(typeof dojo["byId"] == "undefined"){
    dojo.byId = function(id, doc){
        if(id && (typeof id == "string" || id instanceof String)){
            if(!doc){ doc = document; }
            return doc.getElementById(id);
        }
        return id; // assume it's a node
    };
}

dojo._loadUri = function(uri, cb) {
	try{
		if(cb){
			cb(sandbox.loadJS(uri));
		}else{
			sandbox.loadJS(uri);
		}
		return true;
	}catch(e){
		print("load for ('" + uri + "') failed. Exception: " + e.name + " : [" + e.message +"] at line "+e.lineNumber);
		return false;
	}
};

dojo._getText = function(/*URI*/ uri, /*Boolean*/ fail_ok) {
	try{
		var contents = resourceloader.readText(uri);
		if (contents === null) {
			print("Contents of ["+uri+"] is empty");
		}
		return contents;
	}catch(e){
		print("getText('" + uri + "') failed. Exception: " + e);
		if(fail_ok){ return null; }
		throw e;
	}
};

alert = function(msg) {
	print("alert : "+msg);
};
		
dojo.exit = function(exitcode){
    quit(exitcode);
};

// summary:
//      return the document object associated with the dojo.global
dojo.doc = typeof(document) != "undefined" ? document : null;

dojo.body = function(){
    return document.body;
};

dojo.requireIf((dojo.config["isDebug"] || dojo.config["debugAtAllCosts"]), "dojo.debug");

dojo._xhrObj = function(){
	return new XMLHttpRequest();
};

dojo._isDocumentOk = function(http){
	var stat = http.status || 0;
	return (stat >= 200 && stat < 300) || 	// Boolean
		stat == 304 || 						// allow any 2XX response code
		stat == 1223 || 						// get it out of the cache
		(!stat && (location.protocol=="file:" || location.protocol=="chrome:") ); // Internet Explorer mangled the status code
};

dojo.addOnWindowUnload = function() {};

window = document.createWindow();
