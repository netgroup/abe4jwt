<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Login Form</title>
</head>

<body>
<div class="container">
    <p>Login Form</p>
    <hr>
    <form action="login" method="post">
        <table style="with: 50%">
            <tr>
                <td>${requestScope.user}</td>
            </tr>
            <tr>
                <td><input type="text" name="user" placeholder="your email"/></td>
            </tr>
            <tr>
                <td><img src="captcha"/></td>
            </tr>
            <tr>
                <td><input type="text" name="captcha" placeholder="enter captcha"/></td>
            </tr>
            <tr>
                <td><input type="submit"/></td>
            </tr>
        </table>
        <br/>
        ${requestScope.loginMessage}
    </form>

</div>

</body>
</html>