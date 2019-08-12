/** Expand search_resultss **/
document.getElementById("search_button").onclick = function() {expandSearch()};

function expandSearch() {
  document.getElementById("search_results").style.display = "block";
}

/** code by webdevtrick ( https://webdevtrick.com ) **/
$('#rating-form').on('change','[name="rating"]',function(){
	$('#selected-rating').text($('[name="rating"]:checked').val());
});
