<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<meta charset="utf-8">
<title>SuperDiamond 配置管理服务器</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href='<c:url value="/resources/css/bootstrap/css/bootstrap.min.css" />' rel="stylesheet">
<script type="text/javascript" src='<c:url value="/resources/js/jquery.min.js" />'></script>
<script type="text/javascript" src='<c:url value="/resources/js/bootstrap.min.js" />'></script>
<script type="text/javascript" src='<c:url value="/resources/js/bootbox.min.js" />'></script>
<script type="text/javascript" src='<c:url value="/resources/js/bootstrap-paginator.min.js" />'></script>
<script type="text/javascript" src='<c:url value="/resources/js/filesaver.js"/>'></script>
<script type="text/javascript" src='<c:url value="/resources/js/ajaxfileupload.js"/>'></script>
<style type="text/css">
    body {
        padding-top: 60px;
        padding-bottom: 40px;
    }

    .sidebar-nav {
        padding: 9px 0;
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

            <a class="brand" href="javascript: void(0);" style="font-size: 12px; position: relative; bottom: -3px;">Version: <c:out value="${requestScope.get('version')}"></c:out></a>

            <div class="pull-right">
                <p class="navbar-text">
                    欢迎：<c:out value="${sessionScope.sessionUser.userName}"></c:out>&nbsp;&nbsp;&nbsp;
                    <a href='<c:url value="/logout" />'>注销</a>
                </p>
            </div>
        </div>
    </div>
    <a href="https://github.com/talywy/super-diamond"><img style="position: absolute; top: 0; left: 0; border: 0;width: 130px;height: 130px;" src="https://camo.githubusercontent.com/567c3a48d796e2fc06ea80409cc9dd82bf714434/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f6461726b626c75655f3132313632312e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_darkblue_121621.png"></a>
</div>

<div class="container">
    <div class="row-fluid">
        <div class="span2">
            <div class="well sidebar-nav">
                <ul class="nav nav-list">
                    <li class="nav-header"><h3>导航菜单</h3></li>
                    <li id="indexId"><a href='<c:url value="/index" />'>首页</a></li>
                    <c:if test="${sessionScope.sessionUser.userCode == 'admin'}">
                        <li id="userId"><a href='<c:url value="/user/index" />'>用户管理</a></li>
                    </c:if>
                    <li id="projectId"><a href='<c:url value="/project/index" />'>项目管理</a></li>
                    <li id="clientId"><a href='<c:url value="/queryClients" />'>客户端监控</a></li>
                    <li id="passwordId"><a href='<c:url value="/user/password" />'>修改密码</a></li>
                </ul>
            </div>
            <!--/.well -->
        </div>
        <!--/span-->
        <div class="span10">
            <div class="hero-unit" style="padding: 10px;">
                <decorator:body></decorator:body>
            </div>
        </div>
        <!--/span-->
    </div>
    <!--/row-->
</div>
<% request.getSession().removeAttribute("message"); %>
<% request.getSession().removeAttribute("user"); %>
<% request.getSession().removeAttribute("project"); %>
</body>
</html>