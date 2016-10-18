<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
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

        .container, .navbar-static-top .container, .navbar-fixed-top .container, .navbar-fixed-bottom .container {
            width: 1080px;
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

    <a href="https://github.com/talywy/super-diamond"><img style="position: absolute; top: 0; left: 0; border: 0; width: 130px;height: 130px;" src="https://camo.githubusercontent.com/567c3a48d796e2fc06ea80409cc9dd82bf714434/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f6461726b626c75655f3132313632312e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_left_darkblue_121621.png"></a>
</div>

<div class="container">
    <decorator:body></decorator:body>
    <div id="paginator"></div>

    <c:if test="${project.IS_COMMON == '1'}"><img  title="公共项目,可在其他非公共项目中进行配置引用" src="../../resources/images/refrence.png" > </c:if>

    <c:if test="${type != 'build'}" >
        <span class="label label-success">Versions:
            <c:if test="${type=='development'}">
                <c:out value="${project.DEVELOPMENT_VERSION}"/>
            </c:if>
            <c:if test="${type=='production'}">
                <c:out value="${project.PRODUCTION_VERSION}"/>
            </c:if>
            <c:if test="${type=='test'}">
                <c:out value="${project.TEST_VERSION}"/>
            </c:if>
        </span>
    </c:if>
</div>
<% request.getSession().removeAttribute("message"); %>
</body>
</html>
<script type='text/javascript'>

    var moduleId = '<c:out value="${moduleId}" />';
    var recordLimit = '<c:out value="${recordLimit}" />';
    var isShow = '<c:out value="${isShow}"/>';

    var options = {
        size: "small",
        alignment: "right",
        totalPages: <c:out value="${totalPages}"/>,
        currentPage: <c:out value="${currentPage}"/>,
        pageUrl: function (type, page) {
            return "/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>?page=" + page + (moduleId ? '&moduleId=' + moduleId : '') + "&recordLimit=" + recordLimit + "&isShow=" + isShow;
        }
    }
    $('#paginator').bootstrapPaginator(options);
</script>