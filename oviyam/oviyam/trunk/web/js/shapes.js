/**
 * @namespace annotation
 */
ovm.annotation = ovm.annotation || {};

/**
 * shapes.js
 * Definition of shapes
*/

ovm.annotation.Point = function(x,y) {
	this.getX = function() {
		return x;
	};
	this.getY = function() {
		return y;
	};
};

ovm.annotation.Line = function() {
	var len;
	var begin,end;
	var type = "ruler";
	
	this.setLineInfo = function(begin,end,len) {
		this.begin = begin;
		this.end = end;
		this.len  = len;
	}
	this.getBegin = function() {
		return this.begin;
	};
	this.getEnd = function() {
		return this.end;
	};
	this.setBegin = function(begin) {
		this.begin = begin;
	};
	this.setEnd = function(end) {
		this.end = end;
	};
	this.getLength = function() {
		return this.len;
	};
	this.setLength = function(length) {
		this.len = length;
	};	
	this.getType = function() {
		return type;
	};
}; 

ovm.annotation.Rectangle = function() {
	var begin,size;
	var area = 0,mean,stdDev;
	var type = "rect";

	this.setRectInfo = function(begin,size,area,mean,stdDev) {
		this.begin = begin;
		this.size = size;
		this.area = area;
		this.mean = mean;
		this.stdDev = stdDev;
	};
	this.getBegin = function() {
		return this.begin;
	};
	this.setBegin = function(begin) {
		this.begin = begin;
	};
	this.getSize = function() {
		return this.size;
	};
	this.setSize = function(size) {
		this.size = size;
	};
	this.getArea = function() {
		return this.area;
	};
	this.setArea = function(area) {
		this.area = area;
	};
	this.getMean = function() {
		return this.mean;
	};
	this.setMean = function(mean) {
		this.mean = mean;
	};
	this.getStdDev = function() {
		return this.stdDev;
	};
	this.setStdDev = function(stdDev) {
		this.stdDev = stdDev;
	}	
	this.getType = function() {
		return type;
	};
};

ovm.annotation.Oval = function() {
	var center,radius,area,mean,stdDev;	
	var type = "oval";
	this.setOvalInfo = function(center,radius,area,mean,stdDev) {
		this.center = center;
		this.radius = radius;
		this.area = area;
		this.mean = mean;
		this.stdDev = stdDev;
	}
	this.getCenter = function() {
		return this.center;
	};
	this.getRadius = function() {
		return this.radius;
	};
	this.setCenter = function(center) {
		this.center = center;
	};
	this.setRadius = function(radius) {
		this.radius = radius;
	};
	this.getArea = function() {
		return this.area;
	};
	this.setArea = function(area) {
		this.area = area;
	};
	this.getMean = function() {
		return this.mean;
	};
	this.setMean = function(mean) {
		this.mean = mean;
	};
	this.getStdDev = function() {
		return this.stdDev;
	};
	this.setStdDev = function(stdDev) {
		this.stdDev = stdDev;
	};
	this.getType = function() {
		return type;
	};
};

ovm.annotation.Handle = function() {
	var position;

	this.getPosition = function() {
		return this.position;
	};
	this.setPosition = function(position) {
		this.position = position;
	};
};