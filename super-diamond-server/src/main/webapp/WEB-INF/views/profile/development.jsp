<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<a class="brand" href="/superdiamond/index">首页</a> >> <b><c:out value="${project.PROJ_NAME}"/> - <c:out value="${project.PROJ_CODE}"/> - <c:out
        value="${type}"/></b> <br/><br/>

<b>&nbsp;项目模块：</b>
<select id="sel-queryModule">
    <option value="-1">全部</option>
    <c:forEach items="${modules}" var="module">
        <option value='<c:out value="${module.MODULE_ID}"/>'><c:out value="${module.MODULE_NAME}"/></option>
    </c:forEach>
</select>
<!-- <button type="button" id="queryModule" class="btn btn-primary">查询</button> -->
<input id="projectName" type="text" style="display:none" value="<c:out value="${project.PROJ_NAME}"/>"/>
<a id="addModule" href="javascript:void(0)">添加Module</a>
<a id="delModule" href="javascript:void(0)">删除Module</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<input type="checkbox" name="defaultConfig" id="isDefaultConfig">&nbsp;&nbsp;<b>高级配置</b></input>
<div class="pull-right">
    <%@ include file="export.jsp" %>
    <button type="button" id="addConfig" class="btn btn-primary">添加配置</button>
    <button type="button" id="preview" class="btn btn-primary">预览</button>
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
                    <input type="text" id="addModuleName" name="name" class="input-large"><br/>
                    <span id="addTip" style="color: red"></span>
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

                <label class="control-label">是否默认配置：</label>

                <div class="controls">
                    <input type="checkbox" name="isConceal" id="config-isConceal">
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

<div id="updateConfigWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myUpdateModalLabel">参数配置</h3>
    </div>
    <div class="modal-body">
        <form id="updateConfigForm" class="form-horizontal" action='<c:url value="/config/save" />' method="post">
            <div class="control-group">
                <label class="control-label">模块：</label>

                <div class="controls">
                    <select class="input-xxlarge" name="moduleId" id="update-config-moduleId">
                        <option value="">请选择...</option>
                        <c:forEach items="${modules}" var="module">
                            <option value='<c:out value="${module.MODULE_ID}"/>'><c:out
                                    value="${module.MODULE_NAME}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <label class="control-label">Config Key：</label>

                <div class="controls">
                    <input type="hidden" name="configId" id="update-config-configId"/>
                    <input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
                    <input type="hidden" name="type" value='<c:out value="${type}"/>'/>
                    <input type="hidden" name="page" value='<c:out value="${currentPage}"/>'/>
                    <input type="hidden" name="selModuleId" value='<c:out value="${moduleId}"/>'/>
                    <input type="text" name="configKey" class="input-xxlarge" id="update-config-configKey">
                </div>

                <label class="control-label">Config Value：</label>

                <div class="controls">
                    <textarea rows="8" name="configValue" class="input-xxlarge"
                              id="update-config-configValue"></textarea>
                </div>

                <label class="control-label">描述：</label>

                <div class="controls">
                    <textarea rows="2" class="input-xxlarge" name="configDesc" id="update-config-configDesc"></textarea>
                </div>

                <label class="control-label">是否默认配置：</label>

                <div class="controls">
                    <input type="checkbox" name="isConceal" id="update-config-isConceal">
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <span id="updateConfigTip" style="color: red"></span>
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="updateConfig">保存</button>
    </div>
</div>

<div id="viewConfigWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myViewConfigModalLabel">参数配置</h3>
    </div>
    <div class="modal-body">
        <form id="configViewForm" class="form-horizontal">
            <div class="control-group">
                <label class="control-label">模块：</label>

                <div class="controls">
                    <input type="text" name="moduleName" class="input-xxlarge" id="configModuleName"
                           readonly="readonly">
                </div>

                <label class="control-label">Config Key：</label>

                <div class="controls">
                    <input type="text" name="configKey" class="input-xxlarge" id="configConfigKey" readonly="readonly">
                </div>

                <label class="control-label">Config Value：</label>

                <div class="controls">
                    <textarea rows="2" name="configValue" class="input-xxlarge" id="configConfigValue"
                              readonly="readonly"></textarea>
                </div>

                <label class="control-label">Real Config Value：</label>

                <div class="controls">
                    <textarea rows="6" name="realConfigValue" class="input-xxlarge" id="realConfigConfigValue"
                              readonly="readonly"></textarea>
                </div>

                <label class="control-label">描述：</label>

                <div class="controls">
                    <textarea rows="2" class="input-xxlarge" name="configDesc" id="configConfigDesc"
                              readonly="readonly"></textarea>
                </div>

                <label class="control-label">是否默认配置：</label>

                <div class="controls">
                    <input type="checkbox" name="isConceal" id="configIsConceal" readonly="readonly">
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
    </div>
</div>

<div id="moveConfigWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myMoveConfigModalLabel">移动参数配置</h3>
    </div>
    <div class="modal-body">
        <form id="moveConfigForm" class="form-horizontal" action='<c:url value="/config/move" />' method="post">
            <div class="control-group">
                <label class="control-label">移动配置到模块：</label>

                <div class="controls">
                    <select class="input-xxlarge" name="newModuleId" id="moveConfigModuleId">
                        <option value="">请选择...</option>
                        <c:forEach items="${modules}" var="module">
                            <option value='<c:out value="${module.MODULE_ID}"/>'><c:out
                                    value="${module.MODULE_NAME}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="controls">
                    <input type="hidden" name="configId" id="move-config-configId"/>
                    <input type="hidden" name="projectId" value='<c:out value="${projectId}"/>'/>
                    <input type="hidden" name="type" value='<c:out value="${type}"/>'/>
                </div>
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <span id="moveConfigTip" style="color: red"></span>
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="moveConfig">确定</button>
    </div>
</div>


<table class="table table-striped table-bordered">
    <thead>
    <tr>
        <th width="60">Module</th>
        <th width="100">Key</th>
        <th width="70">Config</th>
        <th>Value</th>
        <th width="100">描述</th>
        <th width="45">操作人</th>
        <th width="120">操作时间</th>
        <th width="30">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${configs}" var="config">
        <tr id='row-<c:out value="${config.CONFIG_ID}"/>'
            <c:if test="${fn:startsWith(config.REAL_CONFIG_VALUE, 'RPF:')}"> style=" background: yellow; color: red; font-weight: bold;"  title="配置替换失败" </c:if>>
            <td value='<c:out value="${config.MODULE_ID}"/>'>
                <c:out value="${config.MODULE_NAME}"/>
            </td>
            <td value='<c:out value="${config.CONFIG_KEY}"/>'>
                <c:out value="${config.CONFIG_KEY}"/>
            </td>
            <td title='<c:out value="${config.CONFIG_VALUE}"/>'>
                <script type="text/javascript">
                    var value = '<c:out value="${config.CONFIG_VALUE}"/>';
                    document.write(value.length > 30 ? value.substring(0, 30) + "..." : value);
                </script>
            </td>
            <td title="<c:out value='${fn:replace(config.REAL_CONFIG_VALUE,\"RPF:\",\"\")}' />" >
                <script type="text/javascript">
                    var value = '<c:out value="${config.REAL_CONFIG_VALUE}"/>';
                    if(value.indexOf("RPF:") == 0) {
                        value = value.replace("RPF:","");
                    }
                    document.write(value.length > 30 ? value.substring(0, 30) + "..." : value);
                </script>
            </td>
            <td title='<c:out value="${config.CONFIG_DESC}"/>'>
                    <%--<td title='${config.CONFIG_DESC}'>--%>
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
                <a href='javascript:viewConfig(<c:out value="${config.CONFIG_ID}"/>, <c:out value="${config.IS_SHOW}"/>)'
                   title="查看"><i class="icon-search"></i></a>
                <c:if test="${isAdmin or  sessionScope.sessionUser.userName == 'admin'}">
                    <a class="deleteConfig"
                       href='/superdiamond/config/delete/<c:out value="${config.CONFIG_ID}"/>?projectId=<c:out value="${projectId}"/>&type=<c:out value="${type}"/>&moduleName=<c:out value="${config.MODULE_NAME}"/>'
                       title="删除"><i class="icon-remove"></i></a>
                </c:if>
                <a href='javascript:updateConfig(<c:out value="${config.CONFIG_ID}"/>, <c:out value="${config.IS_SHOW}"/>)'
                   title="更新"><i
                        class="icon-edit"></i></a>
                <a href='javascript:moveConfig(<c:out value="${config.CONFIG_ID}"/>, <c:out value="${config.IS_SHOW}"/>)'
                   title="移动"><i
                        class="icon-move"></i></a>
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

<div style="float: left">
    <b>显示记录数:</b>
    <select id="sel-recordLimit" style="width: 60px">
        <option value="10">10</option>
        <option value="15">15</option>
        <option value="20">20</option>
        <option value="25">25</option>
    </select>
</div>


<script type="text/javascript">

    var gConfigCheckResult;

    function updateConfig(id, isShow) {
        var tds = $("#row-" + id + " > td");

        $("#update-config-moduleId").val($(tds.get(0)).attr("value"));
        $("#update-config-configKey").val($(tds.get(1)).attr("value"));
        $("#update-config-configValue").val($(tds.get(2)).attr("title"));
        $("#update-config-configDesc").val($(tds.get(4)).attr("title"));
        $("#update-config-configId").val(id);
        if (isShow == 0) {
            $("#update-config-isConceal").attr('checked', true);
        } else {
            $("#update-config-isConceal").attr('checked', false);
        }
        $('#updateConfigWin').modal({
            backdrop: false
        })
    }

    function viewConfig(id, isShow) {
        var tds = $("#row-" + id + " > td");
        $("#configModuleName").val($(tds.get(0)).text().trim());
        $("#configConfigKey").val($(tds.get(1)).attr("value"));
        $("#configConfigValue").val($(tds.get(2)).attr("title"));
        $("#realConfigConfigValue").val($(tds.get(3)).attr("title"));
        $("#configConfigDesc").val($(tds.get(4)).attr("title"));
        $("#configIsConceal").val(id);
        if (isShow == 0) {
            $("#configIsConceal").attr('checked', true);
        } else {
            $("#configIsConceal").attr('checked', false);
        }

        $('#viewConfigWin').modal({
            backdrop: false
        })
    }

    function moveConfig(id, isShow) {
        $("#move-config-configId").val(id);
        $('#moveConfigWin').modal({
            backdrop: false
        })
    }

    $(document).ready(function () {
        $("#isDefaultConfig").attr('checked', <c:out value="${isShow}"/>);

        $("#sel-queryModule").val(<c:out value="${moduleId}"/>);

        $("#sel-recordLimit").val(<c:out value="${recordLimit}"/>);

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
            if (!$("#addModuleName").val().trim()) {
                $("#addTip").text("模块名称不能为空");
            } else {
                $("#moduleForm")[0].submit();
            }
        });


        $("#addConfig").click(function (e) {
            $("#config-moduleId").val("");
            $("#config-configKey").val("");
            $("#config-configValue").val("");
            $("#config-configDesc").val("");
            $("#config-isConceal").attr('checked', false);
            $("#configTip").text("");
            $('#addConfigWin').modal({
                backdrop: true
            })
        });

        $("#saveConfig").click(function (e) {
            var re_key = $("#config-configKey").val();
            re_key ="\${" + re_key + "}";
            var key_value = $("#config-configValue").val();
            var re = new RegExp("\\"+re_key);
            if (!$("#config-moduleId").val()) {
                $("#configTip").text("模块不能为空");
            } else if (!$("#config-configKey").val().trim()) {
                $("#configTip").text("configKey不能为空");
            } else if (!$("#config-configValue").val()) {
                $("#configTip").text("configValue不能为空");
            } else if(re.test(key_value)){
                $("#configTip").text("configValue不能引用自身的configKey值");
            }else {
                $("#configForm")[0].submit();
            }
        });

        $("#saveConfigExt").click(function (e) {
            $("#config-flag").val("con");
            var re_key = $("#config-configKey").val();
            re_key ="\${" + re_key + "}";
            var key_value = $("#config-configValue").val();
            var re = new RegExp("\\"+re_key);
            if (!$("#config-moduleId").val()) {
                $("#configTip").text("模块不能为空");
            } else if (!$("#config-configKey").val().trim()) {
                $("#configTip").text("configKey不能为空");
            } else if (!$("#config-configValue").val()) {
                $("#configTip").text("configValue不能为空");
            } else if(re.test(key_value)){
                $("#configTip").text("configValue不能引用自身的configKey值");
            }else {
                $("#configForm")[0].submit();
            }
        });

        $("#updateConfig").click(function (e) {
            var re_key = $("#update-config-configKey").val();
            re_key ="\${" + re_key + "}";
            var key_value = $("update-config-configValue").val();
            var re = new RegExp("\\"+re_key);
            if (!$("#update-config-moduleId").val()) {
                $("#updateConfigTip").text("模块不能为空");
            } else if (!$("#update-config-configKey").val().trim()) {
                $("#updateConfigTip").text("configKey不能为空");
            } else if (!$("#update-config-configValue").val()) {
                $("#updateConfigTip").text("configValue不能为空");
            } else if(re.test(key_value)){
                $("#updateConfigTip").text("configValue不能引用自身的configKey值");
            }else {
                $("#updateConfigForm")[0].submit();
            }
        });

        $("#updateConfigExt").click(function (e) {
            var re_key = $("update-config-configKey").val();
            re_key ="\${" + re_key + "}";
            var key_value = $("#update-config-configValue").val();
            var re = new RegExp("\\"+re_key);
            if (!$("#update-config-moduleId").val()) {
                $("#updateConfigTip").text("模块不能为空");
            } else if (!$("#update-config-configKey").val().trim()) {
                $("#updateConfigTip").text("configKey不能为空");
            } else if (!$("#update-config-configValue").val()) {
                $("#updateConfigTip").text("configValue不能为空");
            }else if(re.test(key_value)){
                $("#updateConfigTip").text("configValue不能引用自身的configKey值");
            } else {
                $("#updateConfigForm")[0].submit();
            }
        });

        $("#moveConfig").click(function (e) {
            if (!$("#moveConfigModuleId").val()) {
                $("#moveConfigTip").text("模块不能为空");
            } else {
                $("#moveConfigForm")[0].submit();
            }
        });

        var flag = "<%= request.getParameter("flag")%>";
        if (flag == "con") {
            $("#addConfig").click();
        }

        var keyFlag = "<%= request.getParameter("keyExistFlag")%>";
        var isShow = "<c:out value="${sessionScope.configObj.isShow}"/>";
        if (keyFlag == "true") {
            $("#config-moduleId").val("<c:out value="${sessionScope.moduleId}"/>")
            $("#config-configKey").val("<c:out value="${sessionScope.configObj.key}"/>");
            $("#config-configValue").val("<c:out value="${sessionScope.configObj.value}"/>");
            $("#config-configDesc").val("<c:out value="${sessionScope.configObj.description}"/>");
            if("<c:out value="${sessionScope.configObj.isShow}"/>" == "true") {
                $("#config-isConceal").attr('checked', true);
            }else{
                $("#config-isConceal").attr('checked', false);
            }
            $("#configTip").text("Config Key在项目中已存在，请更改Config Key！");
            $('#addConfigWin').modal({
                backdrop: false
            })
        }

        var moduleFlag = "<%= request.getParameter("moduleNameExistFlag")%>";
        var moduleName = "<%= session.getAttribute("moduleName")%>";
        if (moduleFlag == "true") {
            $("#addModuleName").val(moduleName);
            $("#addTip").text("添加模块名称在该项目中已存在，请修改模块名称！");
            $("#addModule").click();
        }


        $('#isDefaultConfig').change(function (e) {
            if ($('#isDefaultConfig').is(':checked')) {
                var moduleId = $("#sel-queryModule").val();
                var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>?isShow=true'+"&page=<c:out value="${currentPage}"/>&moduleId=<c:out value="${moduleId}"/>&recordLimit=<c:out value="${recordLimit}" />";
                if (moduleId) {
                    url = url + "&moduleId=" + moduleId;
                }
                window.location.href = url;
            } else {
                var moduleId = $("#sel-queryModule").val();
                var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>?isShow=false'+"&page=<c:out value="${currentPage}"/>&moduleId=<c:out value="${moduleId}"/>&recordLimit=<c:out value="${recordLimit}" />";
                if (moduleId) {
                    url = url + "&moduleId=" + moduleId;
                }
                window.location.href = url;
            }
        });

        $("#sel-recordLimit").change(function (e) {
            var recordLimit = $("#sel-recordLimit").val();
            var moduleId = $("#sel-queryModule").val();
            var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>?page=<c:out value="${currentPage}"/>&isShow=<c:out value="${isShow}"/>&recordLimit=' + recordLimit;
            if (moduleId) {
                url = url + "&moduleId=" + moduleId;
            }
            window.location.href = url;
        });

    });
</script>