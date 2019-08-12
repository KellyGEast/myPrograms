/*------------------------------------------------------
This is the master JS file for project04 html pages 
-------------------------------------------------------*/
/*
   Created  04/23/2019 by Kelly East      .
*/

// setup the footer date
(function(){
	var date = new Date();
	var dateString = date.toDateString();
	document.getElementById('todaysDate').innerHTML = dateString;
})();

// setup the expandable sections

function expandBakery() {
	var bakeryContent = document.getElementById('bakeryContent');
	if (bakeryContent.style.maxHeight){
			bakeryContent.style.maxHeight = null;
		} else {
			  bakeryContent.style.maxHeight = bakeryContent.scrollHeight + "px";
		} 
};

function expandBrunch() {
	var brunchContent = document.getElementById('brunchContent');
	if (brunchContent.style.maxHeight){
			brunchContent.style.maxHeight = null;
		} else {
			  brunchContent.style.maxHeight = brunchContent.scrollHeight + "px";
		} 
};

function expandLunch() {
	var lunchContent = document.getElementById('lunchContent');
	if (lunchContent.style.maxHeight){
			lunchContent.style.maxHeight = null;
		} else {
			  lunchContent.style.maxHeight = lunchContent.scrollHeight + "px";
		} 
};
	
// setup seasonal latte
( function () {
	var date = new Date();
	var month = date.getMonth();
	var day = date.getDate();
	var latteString;
	var spring = "<h2> Spring Latte: Honey Lavender</h2>";
	var summer = "<h2> Summer Latte: Iced Blackberry Earl Grey Tea</h2>";
	var fall = "<h2> Fall Latte: Pumpkin Spice</h2>";
	var winter = "<h2> Winter Latte: Gingerbread</h2>";
	
	if (month < 2) {
		// it is winter
		latteString = winter;
	}
	else if (month == 2) {
		// it is March
		if (day < 20) {
			// it is winter
			latteString = winter;
		}
		else {
			// it is spring
			latteString = spring;
		}
	}
	else if (month > 2 && month < 5) {
		// it is spring
		latteString = spring;
	}
	else if (month == 5) {
		// it is June
		if (day < 21) {
			// it is spring
			latteString = spring;
		}
		else {
			// it is summer
			latteString = summer;
		}
	}
	else if (month > 5 && month < 8) {
		// it is summer
		latteString = summer;
	}
	else if (month == 8) {
		// it is September
		if (day < 23) {
			// it is summer
			latteString = summer;
		}
		else {
			// it is fall
			latteString = fall;
		}
	}
		else if (month > 8 && month < 11) {
		// it is fall
		latteString = fall;
	}
	else {
		// it is December
		if (day < 21) {
			// it is fall
			latteString = fall;
		}
		else {
			// it is winter
			latteString = winter;
		}
	}
	
	document.getElementById('seasonalLatte').innerHTML = latteString;
}) ();
	