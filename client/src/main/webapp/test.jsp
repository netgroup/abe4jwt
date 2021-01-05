
<!DOCTYPE html>
<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>

$(document).ready(function (){
//load cities list  
//	  var results = $.getJSON("cities.json", function(data) {
//	    for(var i = 0; i < data.length; i++) {
//	        $("#cities").append('<option value="' + data[i].name + '">' + data[i].name + '</option');
//	    }
//	  });
	  //update();
	  $("#doTest").click(function(){ 
		  //client.scope=/blog/add/users /blog/set/users/{id}/name /blog/set/users/{id}/country /blog/add/users/{id}/posts /blog/get/users/{id}/posts
			var index;
	    	for (index=0;index<250;index++) {
	    		 $.get("downstream/blog/protected/get/users/"+$("#sub").val()+"/posts")
	    	}
	  });	  
	  $("#submitProfile").click(function(){ 
		    $.ajax({
		    	  type: "POST",
		    	  url: "downstream/blog/protected/set/users/"+$("#sub").val()+"/name",
		    	  data: $("#name").val(),
			      complete:function(response,textStatus){
		    	      alert("Status: " +response.status+" "+textStatus);
		    	      update();
		      	  },
		    	  contentType:"text/plain",
		    	});
	  });
	  $("#submitPost").click(function(){ 
		    $.ajax({
		    	  type: "POST",
		    	  url: "downstream/blog/protected/add/users/"+$("#sub").val()+"/posts",
		    	  data: '{"title":"'+$("#title").val()+'","text":"'+$("#text").val()+'"}',
		    	  //success:function(data,status){
		    	  //    alert("Data: " + data + "\nStatus: " + status);
		    	  //    update();
		    	  //},
			      complete:function(response,textStatus){
			    	      alert("Status: " +response.status+" "+textStatus);
			    	      update();
			      },		    	  
		    	  contentType:"application/json; charset=utf-8",
		    	  dataType: "json"
		    	});
	  });
	  
});

function update() {
	if (!($("#sub").val()==="")) $.get("downstream/blog/protected/get/users/"+$("#sub").val()+"/profile", function(user, status){
		if (status===200&&user.message) { //no error message returned
		    $("#name").val()=user.username;
		    $("#country").val()=user.country;
		} else alert("Status: " +status);
	 });
	
	$.get("downstream/blog/get/users/posts/latest/5",function(data, status) {
						$("#container1").empty();
						for ( const index in data) {
							$("#container1")
									.append(
											'<div class="col-sm-5 wow fadeIn animated" data-wow-delay="0.25s">'
													+ '<img class="float-left" src="img/quotes-1.jpg" alt="Quote 1">'
													+ '<p class="testimonial-desc">'
													+ data[index].title
													+ '</p>'
													+ '<small class="testimonial-author">'
													+ data[index].user.name
													+ '</small>'
													+ '<img class="float-right" src="img/quotes-2.jpg" alt="Quote 2">');
						}
					});
}

function setCookie(cname, cvalue, exdays) {
	  var d = new Date();
	  d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
	  var expires = "expires="+d.toUTCString();
	  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function getCookie(cname) {
	  var name = cname + "=";
	  var ca = document.cookie.split(';');
	  for(var i = 0; i < ca.length; i++) {
	    var c = ca[i];
	    while (c.charAt(0) == ' ') {
	      c = c.substring(1);
	    }
	    if (c.indexOf(name) == 0) {
	      return c.substring(name.length, c.length);
	    }
	  }
	  return "";
}
</script>
</head>

<body>
error: ${requestScope.error}<br>
client's key: ${applicationScope.clientKey}<br>
<a href="authorize">login</a>
<a href="logoff">logoff</a>
jwt: ${sessionScope.jwt}</br>
ephkey: ${sessionScope.ephkey}<br>
sub: <input id="sub" type="text" value="${sessionScope.sub}" disabled="true"/><br>
scope: ${sessionScope.scope}<br> 
	<div class="container fh5co-reviews-inner">
		<div id="container1" class="row justify-content-center"></div>
	</div>
	<form action="" method="post">
		Name: <input type="text" name="name" id="name"><br> 
		Country: <input type="text" name="country" id="country"><br> 
		<!--see https://www.freeformatter.com/iso-country-list-html-select.html-->
		<select id="cities">
   			 <option value="--Select a City--">--Select a City--</option>
		</select>
		<input id="submitProfile" type="button" name="submit" value="submit">
	</form>
	<form action="" method="post">
		Title: <input type="text" name="title" id="title"><br> 
		Text: <input type="text" name="text" id="text"><br> 
		<input id="submitPost" type="button" name="submit" value="submit">
	</form>
	<input id="doTest" type="button" name="doTest" value="doTest">
</body>
</html>
