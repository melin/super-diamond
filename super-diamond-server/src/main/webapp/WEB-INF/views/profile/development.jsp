<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<a class="brand" href="/superdiamond/index">首页</a> >> <b><c:out value="${project.PROJ_NAME}"/> - <c:out value="${type}"/></b> <br/><br/>

<b>模块：</b>
<select id="sel-queryModule">
	<option value="">全部</option>
	<c:forEach items="${modules}" var="module">
		<option value='<c:out value="${module.MODULE_ID}"/>'><c:out value="${module.MODULE_NAME}"/></option>
	</c:forEach>
</select>
<button type="button" id="queryModule" class="btn btn-primary">查询</button>
<a id="addModule" href="javascript:void(0)">添加Module</a>
<a id="delModule" href="javascript:void(0)">删除Module</a>
<!-- <a id="deleteModule" href="javascript:void(0)">删除Module</a> -->
<div class="pull-right">
	<button type="button" id="addConfig" class="btn btn-primary">添加配置</button>
	<button type="button" id="preview" class="btn btn-primary">预览</button>
</div>

<div id="addModalWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    	<h3 id="myModalLabel">添加配置</h3>
  	</div>
  	<div class="modal-body">
    	<form id="moduleForm" class="form-horizontal" action='<c:url value="/module/save" />' method="post">
  			<div class="control-group">
    			<label class="control-label">名称：</label>
    			<div class="controls">
    				<input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
    				<input type="hidden" name="type" value='<c:out value="${type}"/>'/>
      				<input type="text" id="addModuleName" name="name" class="input-large"> <span id="addTip" style="color: red"></span>
    			</div>
  			</div>
		</form>
  	</div>
  	<div class="modal-footer">
    	<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    	<button class="btn btn-primary" id="saveModule">保存</button>
  	</div>
</div>

<div id="addConfigWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    	<h3 id="myModalLabel">添加Module</h3>
  	</div>
  	<div class="modal-body">
    	<form id="configForm" class="form-horizontal" action='<c:url value="/config/save" />' method="post">
  			<div class="control-group">
  				<label class="control-label">模块：</label>
    			<div class="controls">
    				<select class="input-xlarge" name="moduleId" id="config-moduleId">
						<option value="">请选择...</option>
						<c:forEach items="${modules}" var="module">
							<option value='<c:out value="${module.MODULE_ID}"/>'><c:out value="${module.MODULE_NAME}"/></option>
						</c:forEach>
					</select>
    			</div>
    			<label class="control-label">Config Key：</label>
    			<div class="controls">
    				<input type="hidden" name="configId" id="config-configId" />
    				<input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
    				<input type="hidden" name="type" value='<c:out value="${type}"/>'/>
      				<input type="text" name="configKey" class="input-xlarge" id="config-configKey">
    			</div>
    			<label class="control-label">Config Value：</label>
    			<div class="controls">
      				<input type="text" name="configValue" class="input-xlarge" id="config-configValue">
    			</div>
    			<label class="control-label">描述：</label>
    			<div class="controls">
      				<textarea rows="2" class="input-xlarge" name="configDesc" id="config-configDesc"></textarea>
    			</div>
  			</div>
		</form>
  	</div>
  	<div class="modal-footer">
    	<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    	<button class="btn btn-primary" id="saveConfig">保存</button>
  	</div>
</div>

<table class="table table-striped table-bordered">
  	<thead>
    	<tr>
    		<th width="90">Module</th>
      		<th width="120">Key</th>
      		<th>Value</th>
      		<th>描述</th>
      		<th width="45">操作人</th>
      		<th width="120">操作时间</th>
      		<th width="30">操作</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${configs}" var="config">
    		<tr id='row-<c:out value="${config.CONFIG_ID}"/>'>
               	<td value='<c:out value="${config.MODULE_ID}"/>'>
                  	<c:out value="${config.MODULE_NAME}"/>
               	</td>
               	<td value='<c:out value="${config.CONFIG_KEY}"/>'>
               		<c:out value="${config.CONFIG_KEY}"/>
               	</td>
               	<td title='<c:out value="${config.CONFIG_VALUE}"/>' >
                  	<script type="text/javascript">
                  		var value = '<c:out value="${config.CONFIG_VALUE}"/>';
                  		if(value.length > 30)
                  			document.write(value.substring(0, 30) + "...");
                  		else
                  			document.write(value);
                  	</script>
               	</td>
               	<td title='<c:out value="${config.CONFIG_DESC}"/>' >
                  	<script type="text/javascript">
                  		var value = '<c:out value="${config.CONFIG_DESC}"/>';
                  		if(value.length > 15)
                  			document.write(value.substring(0, 15) + "...");
                  		else
                  			document.write(value);
                  	</script>
               	</td>
               	<td>
                  	<c:out value="${config.OPT_USER}"/>
               	</td>
               	<td>
                  	<c:out value="${config.OPT_TIME}"/>
               	</td>
               	<td>
               		<c:if test="${project.OWNER_ID == sessionScope.sessionUser.id}">
                  		<a class="deleteConfig" href='/superdiamond/config/delete/<c:out value="${config.CONFIG_ID}"/>?projectId=<c:out value="${projectId}"/>&type=<c:out value="${type}"/>' title="删除"><i class="icon-remove"></i></a>
                  	</c:if>
                  	<a href='javascript:updateConfig(<c:out value="${config.CONFIG_ID}"/>)' title="更新"><i class="icon-edit"></i></a>
               	</td>
          	</tr>
     	</c:forEach>
	</tbody>
</table>

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<script type="text/javascript">
function updateConfig(id) {
	var tds = $("#row-" + id + " > td");
	$("#config-moduleId").val($(tds.get(0)).attr("value"));
	$("#config-configKey").val($(tds.get(1)).attr("value"));
	$("#config-configValue").val($(tds.get(2)).attr("title"));
	$("#config-configDesc").val($(tds.get(3)).attr("title"));
	$("#config-configId").val(id);
	
	$('#addConfigWin').modal({
		backdrop: false
	})
}

$(document).ready(function () {
	$("#sel-queryModule").val(<c:out value="${moduleId}"/>);
	
	$("#preview").click(function(e) {
		window.location.href = '/superdiamond/profile/preview/<c:out value="${project.PROJ_CODE}"/>/<c:out value="${type}"/>?projectId=<c:out value="${projectId}"/>';
	});
	
	$("a.deleteConfig").click(function(e) {
	    e.preventDefault();
	    bootbox.confirm("确定删除配置项，删除之后不可恢复！", function(confirmed) {
	    	if(confirmed)
	    		window.location.href = $(e.target).parent().attr("href");
	    });
	    
	    return false;
	});
	
	$("#queryModule").click(function(e) {
		var moduleId = $("#sel-queryModule").val();
		var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
		if(moduleId)
			url = url + "?moduleId=" + moduleId;
		
		window.location.href = url;
	});
	
	$("#addModule").click(function(e) {
		$('#addModalWin').modal({
			keyboard: false
		})
	});
	
	$("#delModule").click(function(e) {
		var moduleId = $("#sel-queryModule").val();
		if(moduleId) {
			window.location.href = '/superdiamond/module/delete/<c:out value="${type}"/>/<c:out value="${projectId}"/>/' + moduleId;
		} else {
			bootbox.alert("请选择一个模块！");
		}
	});
	
	$("#saveModule").click(function(e) {
		if(!$("#addModuleName").val()) {
			$("#addTip").text("不能为空");
		} else {
			$("#moduleForm")[0].submit();
		}
	});
	
	$("#addConfig").click(function(e) {
		$('#addConfigWin').modal({
			backdrop: false
		})
	});
	
	$("#saveConfig").click(function(e) {
		$("#configForm")[0].submit();
	});
});
</script>