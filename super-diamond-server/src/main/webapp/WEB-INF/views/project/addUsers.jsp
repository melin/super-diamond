<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<input type="hidden" value="projectId" id="pageId"/>

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
        <tr id="tr-<c:out value="${user.id}" />">
            <td>
                <c:out value="${user.userCode}"/>
            </td>
            <td>
                <c:out value="${user.userName}"/>
            </td>
            <td>
                <c:forEach items="${user.roles}" var="role">
                    <c:out value="${role}"/>&nbsp;&nbsp;
                </c:forEach>
            </td>
            <c:url var="deleteProjectUrl" value="/project/deleteUser">
                <c:param name="projectId" value="${project.ID}"/>
                <c:param name="userId" value="${user.id}"/>
            </c:url>
            <td>
                <c:if test="${user.id != project.OWNER_ID}">
                    <a href="${deleteProjectUrl}" class="deleteProject">删除</a>
                    <a href="javascript:updateAuth('tr-<c:out value="${user.id}" />')">更新</a>
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
        <form class="form-horizontal" method="post" action='<c:url value="/project/saveUser" />' autocomplete="off">
            <input type="hidden" name="userId" value='<c:out value="${user.id}" />'>
            <input type="hidden" name="projectId" value='<c:out value="${project.ID}" />'>
            <tr>
                <td>
                    <c:out value="${user.userCode}"/>
                </td>
                <td>
                    <c:out value="${user.userName}"/>
                </td>
                <td>
                    <%--<input type="checkbox" name="admin" value="admin"> admin &nbsp;--%>
                    <input type="checkbox" name="development" value="development"> development &nbsp;
                    <input type="checkbox" name="test" value="test"> test &nbsp;
                    <input type="checkbox" name="build" value="build"> build &nbsp;
                    <input type="checkbox" name="production" value="production"> production
                </td>
                <td>
                    <button class="btn btn-primary" type="submit">添加用户</button>
                </td>
            </tr>
        </form>
    </c:forEach>
    </tbody>
</table>
<div id="paginator"></div>

<div id="updateAuthWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-heupdateAuthder">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel">更新权限</h3>
    </div>
    <div class="modal-body">
        <form id="moduleForm" class="form-horizontal" action='<c:url value="/project/updateUser" />' method="post">
            <div class="control-group">
                <input type="hidden" name="userId" id="auth-userId">
                <input type="hidden" name="projectId" value='<c:out value="${project.ID}" />'>

                <%--<input type="checkbox" name="admin" value="admin" id="adminId"> admin &nbsp;--%>
                <input type="checkbox" name="development" value="development" id="developmentId"> development &nbsp;
                <input type="checkbox" name="test" value="test" id="testId"> test &nbsp;
                <input type="checkbox" name="build" value="build" id="buildId"> build &nbsp;
                <input type="checkbox" name="production" value="production" id="productionId"> production
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <input type="button" class="btn btn-primary" id="saveAuth" value="保存">
    </div>
</div>

<script type='text/javascript'>
    var options = {
        size: "small",
        alignment: "right",
        totalPages: <c:out value="${totalPages}"/>,
        currentPage: <c:out value="${currentPage}"/>,
        pageUrl: function (type, page, current) {
            return "/superdiamond/project/addUsers?id=<c:out value="${project.ID}" />&page=" + page;
        }
    }
    $('#paginator').bootstrapPaginator(options);

    $("#saveAuth").click(function (e) {
        $("#moduleForm")[0].submit();
    });

    function updateAuth(id) {
        var context = $("#" + id).children("td:eq(2)").text();
        if (context.indexOf("admin") != -1){
            $("#adminId").prop("checked", true);
        }else{
            $("#adminId").removeAttr("checked");
        }
        if (context.indexOf("development") != -1) {
            $("#developmentId").prop("checked", true);
        }else{
            $("#developmentId").removeAttr("checked");
        }
        if (context.indexOf("test") != -1) {
            $("#testId").prop("checked", true);
        }else{
            $("#testId").removeAttr("checked");
        }
        if (context.indexOf("build") != -1) {
            $("#buildId").prop("checked", true);
        }else{
            $("#buildId").removeAttr("checked");
        }
        if (context.indexOf("production") != -1) {
            $("#productionId").prop("checked", true);
        }else{
            $("#productionId").removeAttr("checked");
        }

        $("#auth-userId").val(id.split("-")[1])

        $('#updateAuthWin').modal({
            keyboard: false
        })
    }
</script>