<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="userId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<table class="table table-striped table-bordered">
  	<thead>
    	<tr>
    		<th>用户账号</th>
      		<th>用户名</th>
      		<th width="60">操作</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${users}" var="user">
       		<tr>
               	<td>
                  	<c:out value="${user.userCode}"/>
               	</td>
              	<td>
                  	<c:out value="${user.userName}" />
              	</td>
              	<c:url var="deleteUserUrl" value="/user/delete" >
                  	<c:param name="id" value="${user.id}" />
              	</c:url>
              	<td>
              		<c:if test="${user.userCode != 'admin'}">
              			<a href="${deleteUserUrl}" class="deleteUser">删除</a>
              		</c:if>
              	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>
<ul class="pager">
	<button class="btn btn-primary" onclick='window.location.href = "<c:url value="/user/new" />"'>新建用户</button>
</ul>

<script type="text/javascript">

$(document).ready(function () {
	$("a.deleteUser").click(function(e) {
	    e.preventDefault();
	    bootbox.confirm("确定删除用户，删除之后不可恢复！", function(confirmed) {
	    	if(confirmed)
	    		window.location.href = e.target.href;
	    });
	    
	    return false;
	});
});
</script>