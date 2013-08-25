<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
	<head>
    	<meta charset="utf-8">
    	<title>SuperDiamond 配置管理服务器</title>
    	<meta name="viewport" content="width=device-width, initial-scale=1.0">
    	<link href='<c:url value="/resources/css/bootstrap/css/bootstrap.min.css" />' rel="stylesheet">
    	<script type="text/javascript" src='<c:url value="/resources/js/jquery.min.js" />'></script>
    	<script type="text/javascript" src='<c:url value="/resources/js/bootstrap.min.js" />'></script>
    	<script type="text/javascript" src='<c:url value="/resources/js/bootbox.min.js" />'></script>
    	<style type="text/css">
	      	body {
	        	padding-top: 60px;
	       	 	padding-bottom: 40px;
	      	}
	      	.sidebar-nav {
	       		padding: 9px 0;
	      	}
	      	.table td {
			    line-height: 16px;
			    font-size: 12px;
			    padding: 4px;
			}
			.form-horizontal .control-label {
			 	width: 100px
			}
			
			.form-horizontal .controls {
			    margin-left: 120px;
			}
			.input-xlarge {
				width: 360px
			}
	    </style>
	    <script type="text/javascript">
		  	$(document).ready(function () {
		  		var menuId = $("#pageId").val();
				$("#" + menuId).addClass("active");
		  	});
		</script>
	    <decorator:head/>
	</head>

  	<body>
    	<div class="navbar navbar-inverse navbar-fixed-top">
      		<div class="navbar-inner">
        		<div class="container">
          			<a class="brand" href="/superdiamond/index">SuperDiamond 配置管理服务器</a>
          			<div class="pull-right">
          				<p class="navbar-text">
          					欢迎：<c:out value="${sessionScope.sessionUser.userName}"></c:out>&nbsp;&nbsp;&nbsp;
          					<a href='<c:url value="/logout" />'>注销</a>
          				</p>
          			</div>
        		</div>
      		</div>
    	</div>

    	<div class="container">
			<decorator:body></decorator:body>
			<span class="label label-success">Versions: 
				<c:if test="${type=='development'}">
					<c:out value="${project.DEVELOPMENT_VERSION}" />
				</c:if>
				<c:if test="${type=='production'}">
					<c:out value="${project.PRODUCTION_VERSION}" />
				</c:if>
				<c:if test="${type=='test'}">
					<c:out value="${project.TEST_VERSION}" />
				</c:if>
			</span>
    	</div>
    	<% request.getSession().removeAttribute("message"); %>
  	</body>
</html>