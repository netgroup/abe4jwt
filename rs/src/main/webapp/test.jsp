
<!DOCTYPE html>
<html>
<head>
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script>
$(document).ready(function(){
	  $.get("/users/posts/latest/3", function(data, status){
	    	for (const index in data) {
	    		$("#container1").append(
				'<div class="col-sm-5 wow fadeIn  animated" data-wow-delay="0.25s" style="visibility: visible; animation-delay: 0.25s; animation-name: fadeIn;">'		
				+'<p class="testimonial-desc">'+data[index].title+'</p></div>');
	    	}
	  });
	  $("#submit").click(function(){ 
		    $.ajax({
		    	  type: "POST",
		    	  url: "/users/add",
		    	  data: '{"name":"'+$("#name").val()+'","email":"'+$("#email").val()+'"}',
		    	  success:function(data,status){
		    	      alert("Data: " + data + "\nStatus: " + status);
		    	    },
		    	  contentType:"application/json; charset=utf-8",
		    	  dataType: "json"
		    	});
	  });
	});
</script>
</head>

<body>
	<div class="container fh5co-reviews-inner">
		<div id="container1" class="row justify-content-center"></div>
	</div>
	<form action="" method="post">
		Name: <input type="text" name="name" id="name"><br> 
		Email: <input type="text" name="email" id="email"><br> 
		<input id="submit" type="button" name="submit" value="submit">
	</form>
</body>
</html>
