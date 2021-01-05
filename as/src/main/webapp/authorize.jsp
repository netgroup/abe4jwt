<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<body>

<div class="container">
    <p><h3>${sessionScope.ORIGINAL_PARAMS.user} do you want to Authorize scopes for client : ${sessionScope.ORIGINAL_PARAMS.client}?</h3></p>
    <hr>

    <form method="post" action="authorize">
        <table>
            <tr>
                <td valign="top">Scopes :</td>
                <td>
                    <c:forTokens items="${sessionScope.ORIGINAL_PARAMS.scope[0]}" delims=" " var="a_scope">
                        <input type="checkbox" name="scope" checked="checked" value="${a_scope}">${a_scope}</input><br/>
                    </c:forTokens>
                </td>
            </tr>

            <tr>
                <td colspan="2">
                    <input type="submit" name="approval_status" value="YES"/>
                    <input type="submit" name="approval_status" value="NO"/>
                    <input type="hidden" name="reqId" value="${requestScope.reqId}"/>
                </td>
            </tr>
        </table>
    </form>
</div>
<div class="container">
    <form method="post" action="authorize">
        <input type="submit" name="approval_status" value="I'm not ${sessionScope.ORIGINAL_PARAMS.user}, please log me off."/>
        <input type="hidden" name="reqId" value="${requestScope.reqId}"/>
    </form>
</div>
</body>
</html>
