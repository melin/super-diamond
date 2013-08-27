<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="passwordId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<h2>修改密码</h2>
<form id="passwordForm" class="form-horizontal" method="post" action='<c:url value="/user/updatePassword" />' autocomplete="off" >
	<div class="form-group">
 		<label class="control-label">原密码：</label>
     	<input type="password" class="input-xlarge" id="password" name="password" value='<c:out value="${user.userCode}"/>' > <span id="passwordTip" style="color: red"></span>
   	</div>
   	<div class="form-group">
 		<label class="control-label">新密码：</label>
     	<input type="password" class="input-xlarge" id="newpassword" name="newpassword" value='<c:out value="${user.password}"/>' > <span id="newpasswordTip" style="color: red"></span>
   	</div>
   	<div class="form-group">
 		<label class="control-label">新密码确认：</label>
     	<input type="password" class="input-xlarge" id="renewpassword" name="renewpassword" value='<c:out value="${user.password}"/>' > <span id="renewpasswordTip" style="color: red"></span>
   	</div>
   	<div class="form-actions">
		<button class="btn btn-primary" id="save" type="button">保存</button>
	</div>
</form>

<script type="text/javascript">
$(document).ready(function () {
	$("#save").click(function(e) {
		$("#passwordTip, #newpasswordTip, #renewpasswordTip").text("");
		
		if (!$("#password").val()) {
			$("#passwordTip").text("原密码不能为空");
		} else if ($("#password").val().length<6) {
			$("#passwordTip").text("原密码长度不能小于6");
		} else if (!$("#newpassword").val()) {
			$("#newpasswordTip").text("新密码不能为空");
		} else if ($("#newpassword").val().length<6) {
			$("#newpasswordTip").text("原密码长度不能小于6");
		} else if ($("#newpassword").val() == $("#password").val()) {
			$("#newpasswordTip").text("新密码不能与原密码相同");
		} else if (!$("#renewpassword").val()) {
			$("#renewpasswordTip").text("新密码确认不能为空");
		} else if ($("#newpassword").val() != $("#renewpassword").val()) {
			$("#renewpasswordTip").text("两次输入新密码不一致");
		} else {
			$("#passwordForm")[0].submit();
		}
	});
});
</script>