<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="projectId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<h2>新建项目</h2>
<form class="form-horizontal" method="post" action='<c:url value="/project/save" />' autocomplete="off" >
	<div class="form-group">
 		<label class="control-label">项目编码：</label>
     	<input type="text" class="input-xlarge" name="code" value='<c:out value="${project.code}"/>' >
   	</div>
	<div class="form-group">
 		<label class="control-label">项目名称：</label>
     	<input type="text" class="input-xlarge" name="name" value='<c:out value="${project.name}"/>' >
   	</div>
   	<div class="form-group">
 		<label class="control-label">项目管理者：</label>
     	<input type="text" class="input-xlarge" name="userCode" value='<c:out value="${project.userCode}"/>'>
   	</div>
   	<div class="form-actions">
		<button class="btn btn-primary" type="submit">保存</button>
	</div>
</form>