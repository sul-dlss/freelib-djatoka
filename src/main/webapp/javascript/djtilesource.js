(function( $ ){
    
/**
 * An OpenSeadragon interface for the freelib-djatoka JP2 server.  It is based
 * on Doug Reside's DjatokaSeadragon, but modified to work with the newer fork
 * of OpenSeadragon that's being developed by Chris Thatcher at LoC.
 * 
 * https://github.com/dougreside/DjatokaSeadragon
 * https://github.com/thatcher/openseadragon
 * 
 * @class
 * @extends OpenSeadragon.TileSource
 * @param {string} djatoka
 * @param {string} imageID
 */ 
$.DjTileSource = function(djatoka, imageID) {
	var http = new XMLHttpRequest();
	var tileOverlap = 0;
	var tileSize = 256;
	var minLevel, maxLevel; // handled in TileSource
    
    this.baseURL = djatoka + "zoom/";
    this.imageID = imageID;

    http.open('HEAD', djatoka + "image/" + imageID, false);
    http.send();

    width = parseInt(http.getResponseHeader("X-Image-Width"));
    height = parseInt(http.getResponseHeader("X-Image-Height"));
    
    $.TileSource.call(this, width, height, tileSize, tileOverlap, minLevel, maxLevel);
};

$.extend($.DjTileSource.prototype, $.TileSource.prototype, {
	
    /**
     * @function
     * @name OpenSeadragon.DjTileSource.prototype.getTileUrl
     * @param {Number} level
     * @param {Number} x
     * @param {Number} y
     */
    getTileUrl: function(level, x, y) {
    	var newScale = Math.pow(2, this.maxLevel) / Math.pow(2, level);
    	var tileSize = parseInt(newScale * 256);
    	var tileSizeX, tileSizeY, region;
    	var scale = Math.pow(2, level);

    	if (level > 8) {
    		var myX = parseInt(x);
    		var myY = parseInt(y);

    		if (myX == 0){
    			tileSizeX = tileSize - 1;
    		}
    		else {
    			tileSizeX = tileSize;
    		}

    		if ((startX + tileSizeX) > this.dimensions.x){
    			tileSizeX = this.dimensions.x - startX;
    		}

    		if (myY == 0){
    			tileSizeY = tileSize - 1;
    		}
    		else {
    			tileSizeY = tileSize;
    		}

    		if ((startY + tileSizeY) > this.dimensions.y){
    			tileSizeY = this.dimensions.y - startY;
    		}
     	
    		var startX = parseInt(myX * tileSize);
    		var startY = parseInt(myY * tileSize);    	
    	
    		region = startY + "," + startX + "," + tileSizeY + "," + tileSizeX;
    	}
    	else {
    		region = "";
    	}

    	return this.baseURL + this.imageID + "/" + region + "/" + scale;
    }
});

}(OpenSeadragon));