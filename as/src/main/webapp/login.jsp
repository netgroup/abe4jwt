<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Login Form</title>
</head>
<!-- style>
    input[type=text], input[type=password] {
        width: 75%;
        padding: 4px 0px;
        display: inline-block;
        border: 1px solid #ccc;
        box-sizing: border-box;
    }
    .container {
        width: 50%;
        padding: 16px;
        border: 1px solid #ccc;
        background: beige;
    }

</style-->
<body>

<div class="container">
    <p>Login Form</p>
    <hr>
    <form action="login" method="post">
        <table style="with: 50%">
            <tr>
                <td width="20%">${requestScope.user}</td>
                <td><input type="text" name="user"/></td>
            </tr>
            <tr>
                <td width="20%"></td>
                <td><input type="submit"/></td>
            </tr>
        </table>
        <br/>
        ${requestScope.loginMessage}
    </form>

</div>

</body>
</html>