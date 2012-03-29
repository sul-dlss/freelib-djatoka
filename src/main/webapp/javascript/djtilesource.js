(function($){
    
/**
 * An OpenSeadragon interface for the freelib-djatoka JP2 server.  It is based
 * on Doug Reside's DjatokaSeadragon, but modified to work with the newer
 * OpenSeadragon being developed by Chris Thatcher at Library of Congress.
 * 
 * https://github.com/dougreside/DjatokaSeadragon
 * https://github.com/thatcher/openseadragon
 * 
 * @class
 * @extends OpenSeadragon.TileSource
 * @param {Number} width
 * @param {Number} height
 * @param {Number} tileSize
 * @param {Number} tileOverlap
 * @param {String} minLevel
 * @param {String} maxLevel
 */ 
$.DjTileSource = function(width, height, tileSize, tileOverlap, minLevel, maxLevel) {
    $.TileSource.call(this, width, height, tileSize, tileOverlap, minLevel, maxLevel);
};

$.extend( $.DjTileSource.prototype, $.TileSource.prototype, {
    
    /**
     * @function
     * @name OpenSeadragon.DjTileSource.prototype.getTileUrl
     * @param {Number} level
     * @param {Number} x
     * @param {Number} y
     */
    getTileUrl: function(level, x, y) {
    	var newscale = Math.pow(2, this.maxLevel) / Math.pow(2, level);
    	var tilesize = parseInt(newscale * 256);
    	var dj_scale = "&svc.scale=" + Math.pow(2, level);      	  	
    	var dj_region = "";

    	if (level > 8) {
    		var myX = parseInt(x);
    		var myY = parseInt(y);

    		if (myX == 0){
    			tilesizeX = tilesize - 1;
    		}
    		else {
    			tilesizeX = tilesize;
    		}

    		if ((startx + tilesizeX) > this.dimensions.x){
    			tilesizeX = this.dimensions.x - startx;
    		}

    		if (myY == 0){
    			tilesizeY = tilesize - 1;
    		}
    		else {
    			tilesizeY = tilesize;
    		}

    		if ((starty + tilesizeY) > this.dimensions.y){
    			tilesizeY = this.dimensions.y - starty;
    		}
     	
    		var startx = parseInt(myX * tilesize);
    		var starty = parseInt(myY * tilesize);    	
    	
    		dj_region = "&svc.region=" + starty + "," + startx + "," + tilesizeY + "," + tilesizeX;
    	}

    	return "http://localhost:8888/resolve?url_ver=Z39.88-2004&rft_id=ms0332_gra_21845&svc_id=info:lanl-repo/svc/getRegion&svc_val_fmt=info:ofi/fmt:kev:mtx:jpeg2000&svc.format=image/jpeg&svc.rotate=0" + dj_scale + dj_region; 
    },

    /**
     * @function
     * @name OpenSeadragon.DjTileSource.prototype.tileExists
     * @param {Number} level
     * @param {Number} x
     * @param {Number} y
     */
    /*
    tileExists: function(level, x, y) {
        var rects = this._levelRects[level],
            rect,
            scale,
            xMin,
            yMin,
            xMax,
            yMax,
            i;

        if (!rects || !rects.length) {
            return true;
        }

        for (i = rects.length - 1; i >= 0; i--) {
            rect = rects[i];

            if (level < rect.minLevel || level > rect.maxLevel) {
                continue;
            }

            scale = this.getLevelScale(level);
            xMin = rect.x * scale;
            yMin = rect.y * scale;
            xMax = xMin + rect.width * scale;
            yMax = yMin + rect.height * scale;

            xMin = Math.floor(xMin / this.tileSize);
            yMin = Math.floor(yMin / this.tileSize);
            xMax = Math.ceil(xMax / this.tileSize);
            yMax = Math.ceil(yMax / this.tileSize);

            if (xMin <= x && x < xMax && yMin <= y && y < yMax) {
                return true;
            }
        }

        return false;
    }*/
});

}(OpenSeadragon));