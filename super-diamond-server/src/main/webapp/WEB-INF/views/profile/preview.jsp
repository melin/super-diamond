<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<a class="brand" href="/superdiamond/index">首页</a> >> 
<b> <a href='/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${project.id}"/>'><c:out value="${project.PROJ_NAME}"/> - <c:out value="${type}"/></a> >> 预览
</b> <br/><br/>

<textarea style="width: 940px; height: 500px; font-size: 12px; line-height: 16px;"><c:out value="${message}"/></textarea>
<br/>