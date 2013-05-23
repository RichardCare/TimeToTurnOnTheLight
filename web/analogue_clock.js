// Global variable
var clock_face = null,
	hour_hand = null,
	minute_hand = null,
	second_hand = null,
	ctx = null,
	teams = new Array();

var HEIGHT = 500;
var WIDTH = 500;
var ACTIVITY_NAME = 'activity.jsonp';
var COST_FACTOR = 2;

function init() {
	// Available space
	console.log(window.innerHeight, "by", window.innerWidth);

	// Grab the canvas element
	var canvas = document.getElementById('canvas');
	
	// Canvas supported?
	if (canvas.getContext('2d')) {
		ctx = canvas.getContext('2d');
		
		// Load the hour hand image
		hour_hand = new Image();
  		hour_hand.src = 'hour_hand.png';

		// Load the minute hand image
		minute_hand = new Image();
  		minute_hand.src = 'minute_hand.png';

		// Load the minute hand image
		second_hand = new Image();
  		second_hand.src = 'second_hand.png';
  		
  		// Load the clock face image
		clock_face = new Image();
		clock_face.src = 'clock_face.png';
 		clock_face.onload = imgLoaded;
	} else {
  		alert("Canvas not supported!");
  	}
}

function imgLoaded() {
	// Image loaded event complete. Start the timer which reads ACTIVITY_NAME then calls draw()
	setInterval(reload, 200);
}

function reload() {
	$.getScript(ACTIVITY_NAME, draw);
}

function draw(data, status) {
	// Parse updated activity info
	var now = new Date();
	var activity = eval(activities);

	// Sort activity by time ascending (NB hence has to be objects)
	times = [];
	for (key in activity) {
		var t = new Object();
		t.name = String(key);
		t.time = activity[key];
		times.push(t);
	}
	times.sort(function(a, b) {return a.time - b.time;});

	// Draw clocks in ascending time order
	ctx.clearRect(0, 0, HEIGHT, WIDTH);	 
	var p = new Point(0, 0);
	for (var i = 0; i < times.length; i++) {
		var team = null;
		for (var j = 0; j < teams.length; j++) {
			if (times[i].name == teams[j].teamName) {
				team = teams[j];
				break;
			}
		}
		if (team == null) {
			team = new Clock(times[i].name);
			teams.push(team);
		}
		var piTime = COST_FACTOR * times[i].time;
		team.draw(p, now, piTime);
		p = p.wrap();
	}

}

function Clock(teamName) {
	this.teamName = teamName;
}

Clock.prototype.draw = function(point, now, piTime) {
	var displayTime = new Date(now);
	displayTime.setSeconds(displayTime.getSeconds() + piTime);
	
	// Save the current drawing state & move to top-left of clock
	ctx.save();
	ctx.translate(point.x, point.y);

	// Draw the clock onto the canvas
	ctx.drawImage(clock_face, 0, 0);
	
	// Do the text
	ctx.font="40px Arial";
	ctx.textAlign = 'center';
	ctx.fillStyle = this.teamName;
	ctx.fillText(this.teamName, WIDTH/2, 60);
	ctx.fillText(displayTime.toTimeString().substr(0, 8), WIDTH/2, HEIGHT - 28);
	
	// Now move across and down half the 
	ctx.translate(HEIGHT/2, WIDTH/2);
  	rotateAndDraw(minute_hand, getRequiredAngle(displayTime.getMinutes() + displayTime.getSeconds() / 60, 60));
 	rotateAndDraw(hour_hand, getRequiredAngle(displayTime.getHours() + displayTime.getMinutes() / 60, 12));
 	rotateAndDraw(second_hand, getRequiredAngle(displayTime.getSeconds() + displayTime.getMilliseconds() / 1000, 60));
	
	// Restore the previous drawing state
	ctx.restore();
}

function getRequiredAngle(value, perRev) {
	// Calculate the expected angle
	return value/perRev * 2 * Math.PI;
}

function rotateAndDraw(image, angle) {
	// Rotate around this point
	ctx.rotate(angle);
 
	// Draw the image back and up
	ctx.drawImage(image, -HEIGHT/2, -WIDTH/2);
	
	ctx.rotate(-angle);
}

function Point(x, y) {
	this.x = x;
	this.y = y;
}

Point.prototype.wrap = function() {
	var result = new Point(this.x + WIDTH, this.y);
	if (result.x + WIDTH > window.innerWidth) {
		result.x = 0;
		result.y += HEIGHT;
	}
	return result;
}
