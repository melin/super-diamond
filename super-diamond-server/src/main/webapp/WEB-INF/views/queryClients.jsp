<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="clientId" id="pageId" />

<table class="table table-bordered table-striped">
	<caption>客户端监控</caption>
  	<thead>
    	<tr>
    		<th>项目编码</th>
    		<th>Profile</th>
    		<th>客户端地址</th>
    		<th>连接Server时间</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${clients}" var="client">
       		<tr>
       			<td>
                  	<c:out value="${client.projcode}"/>
               	</td>
               	<td>
                  	<c:out value="${client.profile}"/>
               	</td>
               	<td>
                  	<c:out value="${client.address}"/>
               	</td>
               	<td>
                  	<c:out value="${client.connectTime}"/>
               	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>