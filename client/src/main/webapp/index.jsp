<!DOCTYPE html>
<!--
	App by FreeHTML5.co
	Twitter: http://twitter.com/fh5co
	URL: http://freehtml5.co
-->
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Attribute Based Encryption for JSON Web Token</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description"
	content="Free HTML5 Website Template by FreeHTML5.co" />
<meta name="keywords"
	content="free website templates, free html5, free template, free bootstrap, free website template, html5, css3, mobile first, responsive" />
<meta name="author" content="FreeHTML5.co" />

<!-- Facebook and Twitter integration -->
<meta property="og:title" content="" />
<meta property="og:image" content="" />
<meta property="og:url" content="" />
<meta property="og:site_name" content="" />
<meta property="og:description" content="" />
<meta name="twitter:title" content="" />
<meta name="twitter:image" content="" />
<meta name="twitter:url" content="" />
<meta name="twitter:card" content="" />

<!-- Bootstrap  -->
<link rel="stylesheet" href="css/bootstrap.css">
<!-- Owl Carousel  -->
<link rel="stylesheet" href="css/owl.carousel.css">
<link rel="stylesheet" href="css/owl.theme.default.min.css">
<!-- Animate.css -->
<link rel="stylesheet" href="css/animate.css">
<!-- Font Awesome -->
<link rel="stylesheet"
	href="https://use.fontawesome.com/releases/v5.0.13/css/all.css"
	integrity="sha384-DNOHZ68U8hZfKXOrtjWvjxusGo9WQnrNx2sqG0tfsghAvtVlRW3tvkXWZh58N9jp"
	crossorigin="anonymous">

<!-- Theme style  -->
<link rel="stylesheet" href="css/style.css">
</head>
<body>


<div id="page-wrap">


	<!-- ==========================================================================================================
													   HERO
		 ========================================================================================================== -->

	<div id="fh5co-hero-wrapper">
		<nav class="container navbar navbar-expand-lg main-navbar-nav navbar-light">
			<a class="navbar-brand" href="">ABE4JWT</a>
			<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
			</button>

			<div class="collapse navbar-collapse" id="navbarSupportedContent">
				<ul class="navbar-nav nav-items-center ml-auto mr-auto">
					<li class="nav-item active">
						<a class="nav-link" href="#">Home <span class="sr-only">(current)</span></a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#" onclick="$('#fh5co-features').goTo();return false;">Features</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#" onclick="$('#fh5co-slider').goTo();return false;">Reviews</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#"  onclick="$('#fh5co-download').goTo();return false;">Download</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="${sessionScope.sub==null?'authorize':'logoff'}" id="login">${sessionScope.sub==null?'Login':sessionScope.sub.concat('<br>logoff')}</a>
					</li>	
				</ul>
			</div>
		</nav>

		<div class="container fh5co-hero-inner">
			<h1 class="animated fadeIn wow" data-wow-delay="0.4s">Even Wider Ecosystems...</h1>
			<!-- p class="animated fadeIn wow" data-wow-delay="0.97s">Your Nickname<br/>
			<input type="text" name="name" id="name"></p>
		    <p class="animated fadeIn wow" data-wow-delay="1.27s">Your Country<br/>
		    <input type="text" name="country" id="country"></p-->
		    <p class="animated fadeIn wow" data-wow-delay="0.97s">
			    With the raising of <a href="https://openid.net/connect/">OpenID Connect 1.0</a> and <a href="">JSON Web Token (JWT)</a> as a de facto standard 
			    for token-based authentication, in the last few years the number of identity-centric ecosystems have exponentially increased (together,
			    unfortunately, with the number of potential threats to deployed implementations). Today, JSON Web Tokens (JWT) is so extensively used to 
		    	constitute the second mostly used approach for identifying clients at server side, just after traditional session based authentication!
			    <br>We've slightly altered an OpenID Connect 1.0 web flow to introduce an Attribute Based Encryption (ABE) mechanism that decouples the Authorization Server function 
			    from legacy Resource Servers through cryptographic verification of an ABE policy and a challenge/response interaction with the Client. 				    The resulting distributed authorization-centric mechanism works across different domains and across different identity providers.</p>
		    		
		</div>


	</div>
	
	<!-- ==========================================================================================================
													  FEATURES
		 ========================================================================================================== -->

	<div id="fh5co-features" class="fh5co-advantages-outer" data-wow-delay="0.15s">
	  <div class="container">
		<div class="fh5co-features-inner">
			<div class="row fh5co-features-grid-columns">
				<div class="col-sm-6 in-order-1 sm-6-content wow animated fadeInLeft" data-wow-delay="0.22s">
					<h1>Why does Attribute-Based Encryption concern?</h1>
					<p>
				        Despite its complex math formulation, the concept of ABE is very simple: decryption only happens if the receiver's key embeds 
				        attributes matching a policy the sender has used to encrypt the plaintext. A Resource Server may thus challenge a Client by encrypting a secret and 
				        ask the Client to present it when accessing the service. Only Clients that legitimately got the correct key can decrypt the secret. 
				        The key may embed different attributes (identity information, request context, expiration, etc.) that can be selectively disclosed (depending on the encryption policy).
		        	<p>
		        </div>
				<div class="col-sm-6 in-order-2 sm-6-content wow animated fadeInRight" data-wow-delay="0.22s">
					    <h1>Leave your comment?</h1>
					    <p>Well, you can do that! Comments are intended to provide useful feedback to this work. Please do not post offensive comments.</p>
					    <p class="grid-desc"><input class="col-sm-6" type="text" id="title" placeholder="Title"></p>
					    <p class="grid-desc"><textarea class="col-sm-6" id="text" placeholder="Your comment" ></textarea></p>
						<button class="btn btn-md features-btn-first" data-wow-delay="1.25s" id="cancel" onclick="$('#title').val('');$('#text').val('')">Cancel</button>
						<button class="btn btn-md features-btn-first popup" data-wow-delay="1.25s" id="post">Post!
						<span class="popuptext" id="postError">Did you provide the correct authorization<br>for submitting a post?</span>					
						</button>
				</div>	
			</div>
		</div>
	  </div>	  
	</div>	
	
	<!-- ==========================================================================================================
													  SLIDER
		 ========================================================================================================== -->


	<div id="fh5co-slider" class="fh5co-slider-outer wow fadeIn" data-wow-delay="0.36s" style="visibility: visible; animation-delay: 0.36s; animation-name: fadeIn;">
		<div class="container fh5co-slider-inner">
			<div class="owl-carousel owl-theme">
				<div>
					<p class="testimonial-desc"><b>No comment loaded yet.</b><br>Please <a href="authorize">login</a> for the first time to initialize 
					the application.</p>
				</div>
			</div>

		</div>
	</div>	
	
	<!-- ==========================================================================================================
													 PROFILE
		 ========================================================================================================== -->
	
	<div id="fh5co-advantages" class="fh5co-advantages-outer">
		<div class="container">
			<h1 class="second-title"><span class="span-perfect">Your</span> <span class="span-features">Profile</span></h1>
			<small>You can customize your profile parameters</small>

			<div class="row fh5co-advantages-grid-columns wow animated fadeIn" data-wow-delay="0.36s">

				<div class="col-sm-4">
					<img class="grid-image" src="img/icon-1.png" alt="Icon-1">
					<h1 class="grid-title">Your Email</h1>
					<p class="grid-desc">
					<input type="text" name="email" placeholder="Your Email" id="sub" value="${sessionScope.sub}" disabled="disabled"></br>
					This is your main identity, will not be directly exposed to other users.</p>
				</div>

				<div class="col-sm-4">
					<img class="grid-image" src="img/icon-2.png" alt="Icon-2">
					<h1 class="grid-title">Your Nickname</h1>
					<p class="grid-desc popup">
					<input type="text" placeholder="Your Nickname" id="name"></br>
					<span class="popuptext" id="nameError">Did you provide the correct<br>authorization for changing your nickname?</span>
					We'll show all your comments by nickname.
					</p>
				</div>

				<div class="col-sm-4">
					<img class="grid-image" src="img/icon-3.png" alt="Icon-3">
					<h1 class="grid-title">Your Country</h1>
					<p class="grid-desc popup">
					<input type="text" placeholder="Your Country" id="country"></br>
					<span class="popuptext" id="countryError">Did you provide the correct<br>authorization for changing your Country?</span>					
					If provided, will be shown in your comments.
					</p>
				</div>


			</div>
		</div>
	</div>


	<!-- ==========================================================================================================
                                                 BOTTOM
    ========================================================================================================== -->

	<div id="fh5co-download" class="fh5co-bottom-outer">
		<div class="overlay">
			<div class="container fh5co-bottom-inner">
				<div class="row">
					<div class="col-sm-12">
						<h1>How to download the paper?</h1>
						<p>Our paper is still under review, however, you can request a copy directly to the author via LinkedIn or ResearchGate</p>
						<a class="wow fadeIn animated" data-wow-delay="0.25s" href="https://www.linkedin.com/posts/giovannibartolomeo_encryption-cloud-activity-6758086258146205696-J80c"><img class="app-store-btn" src="img/LinkedIn.png" alt="Linkedin Icon"></a>
						<a class="wow fadeIn animated" data-wow-delay="0.67s" href="https://www.researchgate.net/publication/348372992_Attribute-Based_Encryption_for_Access_Control_in_Cloud_Ecosystems"><img class="google-play-btn" src="img/ResearchGate.png" alt="ResearchGate Icon"></a>
					</div>
				</div>
			</div>
		</div>
	</div>


	<!-- ==========================================================================================================
                                               SECTION 7 - SUB FOOTER
    ========================================================================================================== -->

	<footer class="footer-outer">
		<div class="container footer-inner">

			<div class="footer-three-grid wow fadeIn animated" data-wow-delay="0.66s">
				<div class="column-1-3">
					<h1>ABE4JWT</h1>
				</div>
				<div class="column-2-3">
					<nav class="footer-nav">
						<ul>
							<a href="#" onclick="$('#fh5co-hero-wrapper').goTo();return false;"><li>Home</li></a>
							<a href="#" onclick="$('#fh5co-features').goTo();return false;"><li>Features</li></a>
							<a href="#" onclick="$('#fh5co-slider').goTo();return false;"><li>Reviews</li></a>
							<a href="#" onclick="$('#fh5co-download').goTo();return false;"><li class="active">Download</li></a>
						</ul>
					</nav>
				</div>
				<div class="column-3-3">
					<div class="social-icons-footer">
						<a href="https://www.facebook.com/"><i class="fab fa-facebook-f"></i></a>
						<a href="https://www.instagram.com"><i class="fab fa-instagram"></i></a>
						<a href="https://www.twitter.com/"><i class="fab fa-twitter"></i></a>
					</div>
				</div>
			</div>

			<span class="border-bottom-footer"></span>
			<div class="copyright">In Memory of 
			<div class="popup copyright" id="arianna">Arianna
			<span class="popuptext" id="picture"><img src="img/arianna.jpg"/></span>
			</div>, our Uniroma2 Netgroup's colleague and friend who passed too early.</div>
			<br><div class="copyright">Template designed by <a href="https://freehtml5.co" target="_blank">FreeHTML5</a>.</div>
	</footer>
</div> <!-- main page wrapper -->

  <script src="js/jquery.min.js"></script>
  <script src="js/bootstrap.js"></script>
  <script src="js/owl.carousel.js"></script>
  <script src="js/wow.min.js"></script>
  <script src="js/main.js"></script>
  <script type="text/javascript">
    if (!$("#sub").val()) $("#fh5co-advantages").hide();
    else $.get("downstream/blog/protected/get/users/"+$("#sub").val()+"/profile", function(user, status){
    	//TODO: check this OR use ajax function's "complete" status, see below
		if (user.name) $("#name").val(user.name);
		if (user.country) $("#country").val(user.country);
	 });
    
	 (function($) {
		 function update() {
			//see https://stackoverflow.com/questions/43454419/load-dynamic-content-in-owl-carousel-2
			    var $owl=$(".owl-carousel");
				$.get("downstream/blog/get/users/posts/latest/10",function(data, status) {
					if (status==='success') {
						$owl.trigger("destroy.owl.carousel"); 
						$owl.removeClass("owl-loaded");
						$owl.find(".owl-stage-outer").children().unwrap();
						//var data=$.parseJSON(''+result);
						var content='';
						$.each(data, function(index) {
							content+='<div>'
								+ '<p class="testimonial-desc">'
								+ (data[index].title?'<h1>'+data[index].title+'</h1><br>':'')
								+ data[index].text
								+ '</p>'
								+ '<small class="testimonial-author">'
								+ (data[index].user.name?data[index].user.name:'')
								+ (data[index].user.country?' (from '+data[index].user.country+')':'')
								+ '</small>'
								+'</div>';
						});
						//alert("|"+status+"|"+content+"|");
						$owl.html(content);
						$owl.owlCarousel({
						    margin:10,
						    nav:true,
						    items:2,
						    autoplay:true,
						    loop:true
						});
					}
				});
			}		 
		 
		 update();
		 
		//Change nickname
		  $("#name").change(function(){ 
			    $.ajax({
			    	  type: "POST",
			    	  url: "downstream/blog/protected/set/users/"+$("#sub").val()+"/name",
			    	  data: $("#name").val(),
				      complete:function(response,textStatus){
			    	      update();
			      	  },
			      	  statusCode: {
			      	    401: function() {
			   		      $("#nameError").toggleClass("show");
					      setTimeout(function() { $("#nameError").toggleClass("show")}, 3000);
			      	    }
			      	  },
			    	  contentType:"text/plain",
			    	});
		  });
		  
		  //Change country
		  $("#country").change(function(){ 
			    $.ajax({
			    	  type: "POST",
			    	  url: "downstream/blog/protected/set/users/"+$("#sub").val()+"/country",
			    	  data: $("#country").val(),
				      complete:function(response,textStatus){
			    	      update();
			      	  },
			      	  statusCode: {
				      	    401: function() {
				   		      $("#countryError").toggleClass("show");
						      setTimeout(function() { $("#countryError").toggleClass("show")}, 3000);
				      	    }
				      },
			    	  contentType:"text/plain",
			    	});
		  });		  
		  
		  //Submit a post
		  $("#post").click(function(){ 
			    if ($("#title").val()||$("#text").val()) $.ajax({
			    	  type: "POST",
			    	  url: "downstream/blog/protected/add/users/"+$("#sub").val()+"/posts",
			    	  data: '{"title":"'+$("#title").val()+'","text":"'+$("#text").val()+'"}',
				      complete:function(response,textStatus){
				    	      //alert("Post submit response - status: " +response.status+" "+textStatus);
				    	      update();
				      },
			      	  statusCode: {
				      	    401: function() {
				   		      $("#postError").toggleClass("show");
						      setTimeout(function() { $("#postError").toggleClass("show")}, 3000);
				      	    }
				      },   
			    	  contentType:"application/json; charset=utf-8",
			    	  dataType: "json"
			    	});
		  });		

		  //show picture
	 	  $("#arianna").click(function(){ 
				$("#picture").toggleClass("show");
				setTimeout(function() {if ($("#picture").hasClass("show")) {$("#picture").toggleClass("show")}}, 5000);
	      });

		}(jQuery));
  </script>	
</body>
</html>