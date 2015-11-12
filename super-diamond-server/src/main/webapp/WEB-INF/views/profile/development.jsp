<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<a class="brand" href="/superdiamond/index">首页</a> >> <b><c:out value="${project.PROJ_NAME}"/> - <c:out
        value="${type}"/></b> <br/><br/>

<b>模块：</b>
<select id="sel-queryModule">
    <option value="">全部</option>
    <c:forEach items="${modules}" var="module">
        <option value='<c:out value="${module.MODULE_ID}"/>'><c:out value="${module.MODULE_NAME}"/></option>
    </c:forEach>
</select>
<!-- <button type="button" id="queryModule" class="btn btn-primary">查询</button> -->
<input id="projectName" type="text" style="display:none" value="<c:out value="${project.PROJ_NAME}"/>"/>
<a id="addModule" href="javascript:void(0)">添加Module</a>
<a id="delModule" href="javascript:void(0)">删除Module</a>
<!-- <a id="deleteModule" href="javascript:void(0)">删除Module</a> -->
<div class="pull-right">
    <%--<input type="file" id="fileUpload" accept=".json" onchange="getFilePath()" style="display:none; "/>--%>
    <button type="button" id="importModule" class="btn btn-primary">导入</button>
        <button type="button" id="exportModule" class="btn btn-primary">导出</button>
        <button type="button" id="addConfig" class="btn btn-primary">添加配置</button>
        <button type="button" id="preview" class="btn btn-primary">预览</button>
</div>

<div id="importModuleWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="importLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="importLabel" class="active">导入模块</h3>
    </div>
    <div class="modal-body">
        <form id="importForm" class="form-horizontal"
              action="/superdiamond/module/import/<c:out value='${type}'/>/<c:out value='${projectId}'/>/<c:out value='${currentPage}'/>"
              method="post" enctype="multipart/form-data">
            <div class="control-group">
                请选择要上传的文件：<input id="file" type="FILE" name="file" size="30" accept=".json">
            </div>

        </form>
        <div class="modal-footer">
            <span id="showTip" style="color: red"></span>
            <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button class="btn btn-primary" id="import">确定</button>
        </div>
    </div>
</div>


<div id="exportModuleWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel" class="active">导出模块</h3>
    </div>
    <div class="modal-body">
        <form id="exportForm" class="form-horizontal" action='<c:url value="/module/export" />' method="post">
            <div class="control-group">
                <span><font size="3">请选择相应的模块进行导出：</font></span></br>
                <div class="controls ">
                    <c:forEach items="${modules}" var="module">
                        <div class="checkbox" id="module-count">
                            <label><input type="checkbox" name="moduleCount"
                                          value='<c:out value="${module.MODULE_ID}"/>'>
                                <c:out value="${module.MODULE_NAME}"/>
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <span id="showConfigTip" style="color: red"></span>
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="exportSubmit">确定</button>
    </div>
</div>

<div id="addModalWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabe">添加Module</h3>
    </div>
    <div class="modal-body">
        <form id="moduleForm" class="form-horizontal" action='<c:url value="/module/save" />' method="post">
            <div class="control-group">
                <label class="control-label">名称：</label>

                <div class="controls">
                    <input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
                    <input type="hidden" name="type" value='<c:out value="${type}"/>'/>
                    <input type="hidden" name="page" value='<c:out value="${currentPage}"/>'/>
                    <input type="text" id="addModuleName" name="name" class="input-large"> <span id="addTip"
                                                                                                 style="color: red"></span>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="saveModule">保存</button>
    </div>
</div>

<div id="addConfigWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel1">参数配置</h3>
    </div>
    <div class="modal-body">
        <form id="configForm" class="form-horizontal" action='<c:url value="/config/save" />' method="post">
            <div class="control-group">
                <label class="control-label">模块：</label>

                <div class="controls">
                    <select class="input-xxlarge" name="moduleId" id="config-moduleId">
                        <option value="">请选择...</option>
                        <c:forEach items="${modules}" var="module">
                            <option value='<c:out value="${module.MODULE_ID}"/>'><c:out
                                    value="${module.MODULE_NAME}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <label class="control-label">Config Key：</label>

                <div class="controls">
                    <input type="hidden" name="configId" id="config-configId"/>
                    <input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
                    <input type="hidden" name="type" value='<c:out value="${type}"/>'/>
                    <input type="hidden" name="page" value='<c:out value="${currentPage}"/>'/>
                    <input type="hidden" name="selModuleId" value='<c:out value="${moduleId}"/>'/>
                    <input type="hidden" name="flag" id="config-flag"/>
                    <input type="text" name="configKey" class="input-xxlarge" id="config-configKey">
                </div>
                <label class="control-label">Config Value：</label>

                <div class="controls">
                    <textarea rows="8" name="configValue" class="input-xxlarge" id="config-configValue"></textarea>
                </div>
                <label class="control-label">描述：</label>

                <div class="controls">
                    <textarea rows="2" class="input-xxlarge" name="configDesc" id="config-configDesc"></textarea>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <span id="configTip" style="color: red"></span>
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="saveConfig">保存</button>
        <button class="btn btn-primary" id="saveConfigExt">保存继续添加</button>
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
            <td title='<c:out value="${config.CONFIG_VALUE}"/>'>
                <script type="text/javascript">
                    var value = '<c:out value="${config.CONFIG_VALUE}"/>';
                    if (value.length > 30)
                        document.write(value.substring(0, 30) + "...");
                    else
                        document.write(value);
                </script>
            </td>
            <td title='<c:out value="${config.CONFIG_DESC}"/>'>
                <script type="text/javascript">
                    var value = '<c:out value="${config.CONFIG_DESC}"/>';
                    if (value.length > 15)
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
                    <a class="deleteConfig"
                       href='/superdiamond/config/delete/<c:out value="${config.CONFIG_ID}"/>?projectId=<c:out value="${projectId}"/>&type=<c:out value="${type}"/>&moduleName=<c:out value="${config.MODULE_NAME}"/>'
                       title="删除"><i class="icon-remove"></i></a>
                </c:if>
                <a href='javascript:updateConfig(<c:out value="${config.CONFIG_ID}"/>)' title="更新"><i
                        class="icon-edit"></i></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<c:if test="${sessionScope.message != null}">
    <div class="alert alert-error cle arfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
            ${sessionScope.message}
    </div>
</c:if>

<div class="alert alert-error cle arfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
</div>


<div id="multi-choose" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel2"
     aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel2">导入module</h3>
    </div>
    <div class="modal-body">
        <div class="control-group">
            <label class="control-label">导入的以下配置信息已经存在：</label>
            <div class="controls">
                <div class="controls">
                    <textarea rows="8" name="configValue" class="input-xxlarge" id="message"></textarea>
                </div>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" id="btnSaveCurrent">保留当前</button>
        <button class="btn btn-primary" id="btnCover">覆盖</button>
        <button class="btn" data-dismiss="modal" id="btnCancel" aria-hidden="true">取消</button>
    </div>
</div>


<script type="text/javascript">

    var gConfigCheckResult;

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

        $("#preview").click(function (e) {
            window.location.href = '/superdiamond/profile/preview/<c:out value="${project.PROJ_CODE}"/>/<c:out value="${type}"/>?projectId=<c:out value="${projectId}"/>';
        });

        $("a.deleteConfig").click(function (e) {
            e.preventDefault();
            bootbox.confirm("确定删除配置项，删除之后不可恢复！", function (confirmed) {
                if (confirmed)
                    window.location.href = $(e.target).parent().attr("href");
            });

            return false;
        });

        $("#sel-queryModule").change(function (e) {
            var moduleId = $("#sel-queryModule").val();
            var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
            if (moduleId)
                url = url + "?moduleId=" + moduleId;

            window.location.href = url;
        });

        $("#queryModule").click(function (e) {
            var moduleId = $("#sel-queryModule").val();
            var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
            if (moduleId)
                url = url + "?moduleId=" + moduleId;

            window.location.href = url;
        });

        $("#addModule").click(function (e) {
            $('#addModalWin').modal({
                keyboard: false
            })
        });

        $("#delModule").click(function (e) {
            var moduleId = $("#sel-queryModule").val();
            if (moduleId) {
                window.location.href = '/superdiamond/module/delete/<c:out value="${type}"/>/<c:out value="${projectId}"/>/' + moduleId;
            } else {
                bootbox.alert("请选择一个模块！");
            }
        });

        $("#saveModule").click(function (e) {
            if (!$("#addModuleName").val()) {
                $("#addTip").text("不能为空");
            } else {
                $("#moduleForm")[0].submit();
            }
        });


        $("#addConfig").click(function (e) {
            $('#addConfigWin').modal({
                backdrop: true
            })
        });

        $("#exportModule").click(function (e) {
            $('#exportModuleWin').modal({
                backdrop: true
            })
        });

        $("#importModule").click(function (e) {
            $('#importModuleWin').modal({
                backdrop: true
            })
        });

        $("#cover").click(function (e) {


        });

        $("#import").click(function (e) {
            if (document.getElementById("file").value.toString() == "") {
                $("#showTip").text("文件不能为空");
            }
            else
            {
                ajaxFileUpload();
            }
        });


        function ajaxFileUpload() {
            $.ajaxFileUpload({
                url: "/superdiamond/module/import/<c:out value='${type}'/>/<c:out value='${projectId}'/>/<c:out value='${currentPage}'/>",
                type: 'post',
                secureuri: false,
                fileElementId: 'file',
                dataType: 'text',
                success: function (data, status) {
                    try {
                        var data = JSON ? JSON.parse(data) : eval('(' + data + ')');

                        if (data['checkSuccess']==1) {

                            $('#message').val(data['message']);
                            gConfigCheckResult = data;
                            $('#importModuleWin').modal('hide')
                            $('#multi-choose').modal({
                                backdrop: true
                            })
                        }
                        else if(data['checkSuccess']==2)
                        {
                            window.location.href='/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                        }
                        else {
                            alert('配置检查错误：' + data.message);
                        }
                    } catch (e) {
                        alert("error, " + e.message);
                    }
                },
                error: function (data, status, e) {
                    alert(data);
                }
            });

            $("#btnSaveCurrent").click(function (e) {
                performImport(gConfigCheckResult['checkId'], 1)
            });

            $("#btnCover").click(function () {
                performImport(gConfigCheckResult['checkId'], 2)
            });

            $("#btnCancel").click(function () {
                performImport(gConfigCheckResult['checkId'], 3)
            });
        }

        /**
         * 执行导入操作
         * @param {String} checkId 配置检查ID
         * @param {number} operation 操作 1-保存当前 2-覆盖 3-取消
         */
        function performImport(checkId, operation) {
            $.ajax({
                type: "POST",
                url: "/superdiamond/module/import/perform/" + checkId + "/" + operation + "/<c:out value="${projectId}"/>/<c:out value="${type}"/>",
                success: function (data, status) {
                    if(data=="success")
                    {
                        window.location.href='/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                    }
                    else
                    {
                        window.location.href='/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                    }
                },
                error: function (data, status, e) {
                    alert(data);
                    window.location.href='/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';

                }
            });
        }

        function chk() {
            var obj = document.getElementsByName('moduleCount'); //选择所有name="'moduleCount'"的对象，返回数组取到对象数组后，我们来循环检测它是不是被选中
            var checkTrue = new Array();
            var j = 0;
            for (var i = 0; i < obj.length; i++) {
                if (obj[i].checked) {
                    checkTrue[j] = obj[i].value;
                    j++;
                }
            }
            return checkTrue;
        }

        function getJson(URL) {
            var jsonString = null;
            $.ajax({
                type: "get",
                async: false,
                url: URL,
                dataType: "json",
                success: function (data) {
                    jsonString = data;
                    document.location.href = 'redirect:/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                },
            });
            return jsonString;
        }

        $("#exportSubmit").click(function (e) {
            var moduleIds = new Array();
            moduleIds = chk();
            if (moduleIds.length == 0) {
                $("#showConfigTip").text("模块不能为空");
            }
            else {
                window.location.href = '/superdiamond/module/export/<c:out value="${type}"/>/<c:out value="${projectId}"/>/<c:out value="${sessionScope.sessionUser.userName}"/>/' + moduleIds;
                var URL = '/superdiamond/module/export/<c:out value="${type}"/>/<c:out value="${projectId}"/>/<c:out value="${sessionScope.sessionUser.userName}"/>/' + moduleIds;
                var jsonData = getJson(URL);
                exportJson(jsonData);
            }
        });

        function exportJson(jsonString) {
            var jsonF = JSON.stringify(jsonString, null, 2);
            var jsonFormat = [jsonF];
            var blob = new Blob(jsonFormat, {type: 'application/json'});
            var name = document.getElementById("projectName").value.toString();
            saveAs(blob, name + ".json");
        }

        $("#saveConfig").click(function (e) {
            if (!$("#config-moduleId").val()) {
                $("#configTip").text("模块不能为空");
            } else if (!$("#config-configKey").val()) {
                $("#configTip").text("configKey不能为空");
            } else if (!$("#config-configValue").val()) {
                $("#configTip").text("configValue不能为空");
            } else {
                $("#configForm")[0].submit();
            }
        });

        $("#saveConfigExt").click(function (e) {
            $("#config-flag").val("con");
            if (!$("#config-moduleId").val()) {
                $("#configTip").text("模块不能为空");
            } else if (!$("#config-configKey").val()) {
                $("#configTip").text("configKey不能为空");
            } else if (!$("#config-configValue").val()) {
                $("#configTip").text("configValue不能为空");
            } else {
                $("#configForm")[0].submit();
            }
        });

        var flag = "<%= request.getParameter("flag")%>";
        if (flag == "con") {
            $("#addConfig").click();
        }
    });
</script>