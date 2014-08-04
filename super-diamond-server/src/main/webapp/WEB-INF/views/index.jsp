<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="indexId" id="pageId" />

<table class="table table-bordered table-striped">
	<caption>我的项目</caption>
  	<thead>
    	<tr>
    		<th>项目编码</th>
    		<th>项目名称</th>
    		<th>Profiles</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${projects}" var="project">
       		<tr>
       			<td>
                  	<c:out value="${project.code}"/>
               	</td>
               	<td>
                  	<c:out value="${project.name}"/>
               	</td>
               	<td>
               		<c:if test="${sessionScope.sessionUser.userCode == 'admin'}">
               			<a href='./profile/development/<c:out value="${project.id}"/>'>development</a>&nbsp;
               			<a href='./profile/test/<c:out value="${project.id}"/>'>test</a>&nbsp;
               			<a href='./profile/build/<c:out value="${project.id}"/>'>build</a>&nbsp;
               			<a href='./profile/production/<c:out value="${project.id}"/>'>production</a>&nbsp;
               		</c:if>
               		<c:if test="${sessionScope.sessionUser.userCode != 'admin'}">
	               		<c:forEach items="${project.roles}" var="role">
	               			<c:if test="${role == 'development'}"> 
							 	<a href='./profile/development/<c:out value="${project.id}"/>'>development</a>&nbsp;
							</c:if>
							<c:if test="${role == 'test'}"> 
							 	<a href='./profile/test/<c:out value="${project.id}"/>'>test</a>&nbsp;
							</c:if>
							<c:if test="${role == 'build'}"> 
							 	<a href='./profile/build/<c:out value="${project.id}"/>'>build</a>&nbsp;
							</c:if>
							<c:if test="${role == 'production'}"> 
							 	<a href='./profile/production/<c:out value="${project.id}"/>'>production</a>&nbsp;
							</c:if>
	                  	</c:forEach>
                  	</c:if>
               	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>
<div id="paginator"></div>

<script type='text/javascript'>
    var options = {
    	size: "small",
    	alignment:"right",
    	totalPages: <c:out value="${totalPages}"/>,
        currentPage: <c:out value="${currentPage}"/>,
        pageUrl: function(type, page, current){
            return "/superdiamond/index?page="+page;
        }
    }
    $('#paginator').bootstrapPaginator(options);
</script>