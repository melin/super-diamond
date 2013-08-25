<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="userId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<h2>新建用户</h2>
<form class="form-horizontal" method="post" action='<c:url value="/user/save" />' autocomplete="off" >
	<div class="form-group">
 		<label class="control-label">登录账号：</label>
     	<input type="text" class="input-xlarge" name="userCode" value='<c:out value="${user.userCode}"/>' >
   	</div>
   	<div class="form-group">
 		<label class="control-label">用户名：</label>
     	<input type="text" class="input-xlarge" name="userName" value='<c:out value="${user.userName}"/>' >
   	</div>
   	<div class="form-group">
 		<label class="control-label">密码：</label>
     	<input type="text" class="input-xlarge" name="password" value='<c:out value="${user.password}"/>' >
   	</div>
   	<div class="form-actions">
		<button class="btn btn-primary" type="submit">保存</button>
	</div>
</form>