/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
loadJS = function(moduleName) {
    return sandbox.loadJS(moduleName);
};
readText = function(filePath) {
    return resourceloader.readText(filePath);
};
XMLHttpRequest = function() {
	this.headers = {};
	this.responseHeaders = {};
};

XMLHttpRequest.prototype = {
	open: function(method, url, async, user, password){ 
		this.readyState = 1;
		if (async) {
			this.async = true;
		}
		this.method = method || "GET";
		this.url = url;
		this.onreadystatechange();
	},
	setRequestHeader: function(header, value){
		this.headers[header] = value;
	},
	getResponseHeader: function(header){ },
	send: function(data){
		var hdrs = (this.headers === undefined ? [] : this.headers);
		var requestURL = url.parse(this.url);
		var query = requestURL.query || "";
		if (query !== "") {
			query = "?"+query;
		}
		var port = requestURL.hostname === undefined ? 8080 : requestURL.port || 80;
		var options = {
			host: requestURL.hostname || "localhost",
			port: port,
			path: requestURL.pathname + query,
			method: this.method
		};
		//console.log(JSON.stringify(options));
		var scope = this;
		var request = http.request(options, function(res) {
			scope.status = res.statusCode;
			scope.statusText = "";
			scope.readyState = 3;
			scope.onreadystatechange();
		}).on('error', function(e) {
			scope.status = e.statusCode;
			scope.statusText = "Error";
			scope.readyState = 4;
			scope.onreadystatechange();
		}).on('response', function (response) {
			response.on('data', function (chunk) {
				scope.responseText += chunk;
			});
			response.on('end', function () {
				scope.readyState = 4;
				scope.onreadystatechange();
			});
		});		
		request.end();
		this.readyState = 2;
		this.onreadystatechange();
	},
	abort: function(){},
	onreadystatechange: function(){},
	getResponseHeader: function(header){
		if (this.readyState < 3) {
			throw new Error("INVALID_STATE_ERR");
		}
		else {
			var returnedHeaders = [];
			for (var rHeader in this.responseHeaders) {
				if (rHeader.match(new Regexp(header, "i"))) {
					returnedHeaders.push(this.responseHeaders[rHeader]);
				}
			}
			
			if (returnedHeaders.length) {
				return returnedHeaders.join(", ");
			}
		}
		
		return null;
	},
	getAllResponseHeaders: function(header){
		if (this.readyState < 3) {
			throw new Error("INVALID_STATE_ERR");
		}
		else {
			var returnedHeaders = [];
			
			for (var hdr in this.responseHeaders) {
				returnedHeaders.push(hdr + ": " + this.responseHeaders[hdr]);
			}
			
			return returnedHeaders.join("\r\n");
		}
	},
	async: false,
	readyState: 0,
	responseText: "",
	status: 0
};

djConfig = {
	isDebug: false,
	usePlainJson: true,
	baseUrl: "/dojo/"
};

sandbox.loadJS("dojo/_base/_loader/bootstrap.js");

dojo._hasResource = {};
sandbox.loadJS("dojo/_base/_loader/loader.js");

navigator = {
	get userAgent(){
		return "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.8.1.3) Gecko/20070309 Firefox/2.0.0.3";
	}
};
sandbox.loadJS(hostfile);
sandbox.loadJS("dojo/_base.js");
