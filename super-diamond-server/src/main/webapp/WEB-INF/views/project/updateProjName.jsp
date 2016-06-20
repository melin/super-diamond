<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>


<c:if test="${sessionScope.message != null}">
    <div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
            ${sessionScope.message}
    </div>
</c:if>
<h2>项目名称更新</h2>
<c:url var="destinationUrl" value="/project/index">
    <c:param name="page" value="${sessionScope.page}"/>
</c:url>
<form class="form-horizontal"  method="post" action='<c:url value="/project/saveUpdate" />' autocomplete="off" id ="projectForm">
    <input type="hidden" name="id"  value='<c:out value="${project.id}"/>'/>
    <div class="form-group">
        <label class="control-label">项目编码：</label>
        <input type="text" class="input-xlarge" name="code" value='<c:out value="${project.code}"/>' id="code">
        <span id="codeTip" style="color: red"></span>

    </div>
    <div class="form-group">
        <label class="control-label">项目名称：</label>
        <input type="text" class="input-xlarge" name="name" value='<c:out value="${project.name}"/>' id="name">
        <span id="nameTip" style="color: red"></span>

    </div>
    <div class="form-group">
        <label class="control-label">项目管理者：</label>
        <input type="text" class="input-xlarge" name="userCode" value='<c:out value="${project.userCode}"/>' id="userCode">
        <span id="userCodeTip" style="color: red"></span>

    </div>
    <div class="form-actions">
        <button class="btn btn-primary" type="button" id="update">更新</button>
        <a href='${destinationUrl}' class="btn btn-primary ">返回</a>
    </div>
</form>
<script type="text/javascript">
    $(document).ready(function () {
        $("#update").click(function (e) {
            $("#codeTip, #nameTip, #userCodeTip").text("");

            if (!$("#code").val().trim()) {
                $("#codeTip").text("项目编码不能为空");
            } else if (!$("#name").val().trim()) {
                $("#nameTip").text("项目名称不能为空");
            } else if (!$("#userCode").val().trim()) {
                $("#userCodeTip").text("项目管理者不能为空");
            } else {
                $("#projectForm")[0].submit();
            }
        });
    });
</script>

