<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="projectId" id="pageId" />

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<table class="table table-striped table-bordered">
	<caption class="text-left">项目成员</caption>
  	<thead>
    	<tr>
    		<th>登录账号</th>
      		<th>用户名</th>
      		<th>权限</th>
      		<th>操作</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${projUsers}" var="user">
       		<tr>
               	<td>
                  	<c:out value="${user.userCode}"/>
               	</td>
              	<td>
                  	<c:out value="${user.userName}" />
              	</td>
              	<td>
              		<c:forEach items="${user.roles}" var="role">
                  		<c:out value="${role}" />&nbsp;&nbsp;
                  	</c:forEach>
              	</td>
              	<c:url var="deleteProjectUrl" value="/project/deleteUser" >
                  	<c:param name="projectId" value="${project.ID}" />
                  	<c:param name="userId" value="${user.id}" />
              	</c:url>
              	<td>
              		<c:if test="${user.id != project.OWNER_ID}">
              			<a href="${deleteProjectUrl}" class="deleteProject">删除</a>
              		</c:if>
              	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>

<table class="table table-striped table-bordered">
	<caption class="text-left">系统用户</caption>
  	<thead>
    	<tr>
    		<th>登录账号</th>
      		<th>用户名</th>
      		<th>权限</th>
      		<th>操作</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${users}" var="user">
    		<form class="form-horizontal" method="post" action='<c:url value="/project/saveUser" />' autocomplete="off" >
    			<input type="hidden" name="userId" value='<c:out value="${user.id}" />'>
    			<input type="hidden" name="projectId" value='<c:out value="${project.ID}" />'>
	       		<tr>
	               	<td>
	                  	<c:out value="${user.userCode}"/>
	               	</td>
	              	<td>
	                  	<c:out value="${user.userName}" />
	              	</td>
	              	<td>
	              		<input type="checkbox" name="admin" value="admin" id="adminId"> admin &nbsp;&nbsp;
	        			<input type="checkbox" name="development" value="development" id="developmentId"> development &nbsp;&nbsp;
	        			<input type="checkbox" name="test" value="test" id="testId"> test &nbsp;&nbsp;
	        			<input type="checkbox" name="production" value="production" id="productionId"> production
	              	</td>
	              	<td>
	              		<button class="btn btn-primary" type="submit">添加用户</button>
	              	</td>
	            </tr>
            </form>
     	</c:forEach>
	</tbody>
</table>