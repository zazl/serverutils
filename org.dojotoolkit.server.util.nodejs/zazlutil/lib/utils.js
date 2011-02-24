/*
    Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/
exports.getBestFitLocale = function(localeHeader) {
    var localeRegex = /([a-z]{1,8}(-[a-z]{1,8})?)\s*(;\s*q\s*=\s*(1|0\.[0-9]+))?/i;
    var locales = localeHeader.split(',');
    var bestFitQfactor = 0;
    var bestFitLocale = "";
    for (var i = 0; i < locales.length; i++) {
        var matches = localeRegex.exec(locales[i]);
        var qfactor = parseFloat(matches[4]) || 1.0;
        if (qfactor > bestFitQfactor) {
        	bestFitQfactor = qfactor;
        	bestFitLocale = matches[1];
        }
    }
	return bestFitLocale;
};
