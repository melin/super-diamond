<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="userId" id="pageId"/>

<c:if test="${sessionScope.message != null}">
    <div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
            ${sessionScope.message}
    </div>
</c:if>

<h2>新建用户</h2>

<form id="userForm" class="form-horizontal" method="post" action='<c:url value="/user/save" />' autocomplete="off">
    <div class="form-group">
        <label class="control-label">登录账号：</label>
        <input type="text" class="input-xlarge" id="userCode" name="userCode" value='<c:out value="${user.userCode}"/>'>
        <span id="userCodeTip" style="color: red"></span>
    </div>
    <div class="form-group">
        <label class="control-label">用户名：</label>
        <input type="text" class="input-xlarge" id="userName" name="userName" value='<c:out value="${user.userName}"/>'>
        <span id="userNameTip" style="color: red"></span>
    </div>
    <div class="form-group">
        <label class="control-label">密码：</label>
        <input type="password" class="input-xlarge" id="password" name="password"
               value='<c:out value="${user.password}"/>'> <span id="passwordTip" style="color: red"></span>
    </div>
    <div class="form-group">
        <label class="control-label">密码确认：</label>
        <input type="password" class="input-xlarge" id="repassword" name="repassword"
               value='<c:out value="${user.password}"/>'> <span id="repasswordTip" style="color: red"></span>
    </div>
    <div class="form-actions">
        <button class="btn btn-primary" id="save" type="button">保存</button>
    </div>
</form>

<script type="text/javascript">
    $(document).ready(function () {
        $("#save").click(function (e) {
            $("#userCodeTip, #userNameTip, #passwordTip, #repasswordTip").text("");

            if (!$("#userCode").val().trim()) {
                $("#userCodeTip").text("登录账号不能为空");
            } else if (!$("#userName").val().trim()) {
                $("#userNameTip").text("用户名不能为空");
            } else if (!$("#password").val().trim()) {
                $("#passwordTip").text("密码不能为空");
            } else if ($("#password").val().length < 6) {
                $("#passwordTip").text("密码长度不能小于6");
            } else if (!$("#repassword").val().trim()) {
                $("#repasswordTip").text("密码确认不能为空");
            } else if ($("#repassword").val() != $("#password").val()) {
                $("#repasswordTip").text("两次输入密码不一致");
            } else {
                $("#userForm")[0].submit();
            }
        });

        <%--var userExistFlag = "<c:out value="${requestScope.userExistFlag}"/>";--%>
        var userExistFlag = "<%= request.getParameter("userExistFlag")%>"
        debugger
        if (userExistFlag == "true") {
            $("#userCodeTip").text("登录账号在系统已注册，请更改账号！");
        }
    });
</script>