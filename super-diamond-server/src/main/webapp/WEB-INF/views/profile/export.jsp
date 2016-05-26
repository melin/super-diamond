<%@ page import="com.alibaba.fastjson.JSON" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% String reuUri = request.getRequestURI();
    if (reuUri.indexOf("profile/development") > 0) { %>
<button type="button" id="importModule" class="btn btn-primary">导入</button>
<% } %>
<button type="button" id="exportModule" class="btn btn-primary">导出</button>

<input id="projectName" type="text" style="display:none" value="<c:out value="${project.PROJ_NAME}"/>"/>

<div id="importModuleWin" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="importLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="importLabel" class="active">导入模块</h3>
    </div>
    <div class="modal-body">
        <form id="importForm" class="form-horizontal"
              action="<c:url value="/module/import/${type}/${projectId}/${currentPage}" />"
              method="post" enctype="multipart/form-data">
            <div class="control-group">
                请选择要上传的文件：<input id="file" type="FILE" name="file" size="30" accept=".json,.properties">
            </div>
        </form>
    </div>
    <div class="modal-footer">
        <span id="showTip" style="color: red"></span>
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
        <button class="btn btn-primary" id="import">确定</button>
    </div>
</div>


<div id="exportModuleWin" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel" class="active">导出模块</h3>
    </div>
    <div class="modal-body">
        <form id="exportForm" class="form-horizontal" action='<c:url value="/module/export" />' method="post">
            <div class="control-group">
                <div class="alert alert-info">请选择相应的模块进行导出：</div>
                <div class="control-group" id="modules-group">
                    <c:forEach items="${modules}" var="module">
                        <div class="checkbox">
                            <label><input type="checkbox" value='<c:out value="${module.MODULE_ID}"/>'>
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

        <div class="pull-left">
            <button class="btn btn-primary" id="checkSelector" value="全选">全选</button>
        </div>
        <div class="pull-right">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
            <button class="btn btn-primary" id="exportSubmit">导出json</button>
            <button class="btn btn-primary" id="exportProperties">导出properties</button>
        </div>
    </div>
</div>

<script type="text/javascript">

    $(function () {

        var _modules = $("#modules-group").find('input[type=checkbox]');
        var _checkSelector = $("#checkSelector");

        _modules.change(function () {
            if (!$(this).is(':checked')) {
                _checkSelector.text('全选');
            }
        })

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

        _checkSelector.click(function (e) {

            var checkedCount = 0;

            $.each(_modules, function (index, ele) {
                if ($(ele).is(':checked')) {
                    checkedCount++;
                }
            })

            if (checkedCount < _modules.length) {
                _modules.prop('checked', true);
                $(this).text('反选');
            } else {
                _modules.prop('checked', false);
                $(this).text('全选');
            }
        });

        $("#import").click(function (e) {
            if ($.trim($('#file').val()) == '') {
                $("#showTip").text("文件不能为空");
            } else {
                ajaxFileUpload();
            }
        });

        function ajaxFileUpload() {
            //if(document.getElementById("file").value.toString().indexOf(".json")>0)
            $.ajaxFileUpload({
                url: "<c:url value="/module/import/${type}/${projectId}/${currentPage}" />",
                type: 'post',
                secureuri: false,
                fileElementId: 'file',
                dataType: 'text',
                success: function (data, status) {
                    try {
                        var dataDecode = decodeURIComponent(decodeURIComponent(escape(data)));
                        var data = JSON ? JSON.parse(dataDecode) : eval('(' + dataDecode + ')');
                        if (data['checkSuccess'] == 1) {

                            $('#message').val(data['message']);
                            gConfigCheckResult = data;
                            $('#importModuleWin').modal('hide')
                            $('#multi-choose').modal({
                                backdrop: true
                            })
                        } else if (data['checkSuccess'] == 2) {
                            window.location.href = '/superdiamond/profile/${type}/${projectId}';
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
        }

        $("#btnSaveCurrent").click(function (e) {
            performImport(gConfigCheckResult['checkId'], 1)
        });

        $("#btnCover").click(function () {
            performImport(gConfigCheckResult['checkId'], 2)
        });

        $("#btnCancel").click(function () {
            performImport(gConfigCheckResult['checkId'], 3)
        });

        /**
         * 执行导入操作
         * @param {String} checkId 配置检查ID
         * @param {number} operation 操作 1-保存当前 2-覆盖 3-取消
         */
        function performImport(checkId, operation) {
            $.ajax({
                type: "POST",
                url: "/superdiamond/module/import/perform/" + checkId + "/" + operation + "/${projectId}/${type}",
                success: function (data, status) {
                    if (data == "success") {
                        window.location.href = '/superdiamond/profile/${type}/${projectId}';
                    }
                    else {
                        window.location.href = '/superdiamond/profile/${type}/${projectId}';
                    }
                },
                error: function (data, status, e) {
                    alert(data);
                    window.location.href = '/superdiamond/profile/${type}/${projectId}';
                }
            });
        }

        function getCheckedModules() {
            var modules = [];
            $.each(_modules, function (index, ele) {
                if ($(ele).is(':checked')) {
                    modules.push($(ele).val());
                }
            })
            return modules;
        }

        $("#exportSubmit").click(function (e) {
            var moduleIds = getCheckedModules();
            if (moduleIds.length == 0) {
                $("#showConfigTip").text("模块不能为空");
            }
            else {
                var URL = '/superdiamond/module/exportJson/${type}/${projectId}/${sessionScope.sessionUser.userName}/' + moduleIds;
                var jsonData = getJson(URL);
                exportJson(jsonData);
                window.location.href = '/superdiamond/profile/${type}/${projectId}';
            }
        });

        function getJson(URL) {
            debugger
            var jsonString = null;
            $.ajax({
                type: "get",
                async: false,
                url: URL,
                dataType: "text",
                success: function (data) {
                    jsonString = decodeURIComponent(data);;
                    document.location.href = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                },
            });
            return jsonString;
        }

        function exportJson(jsonString) {
            var jsonStr = JSON.parse(jsonString);
            var jsonF = JSON.stringify(jsonStr, null, 2);
            var jsonFormat = [jsonF];
            var blob = new Blob(jsonFormat, {type: 'application/json'});
            var name = document.getElementById("projectName").value.toString();
            saveAs(blob, name + ".json");
        }

        $("#exportProperties").click(function (e) {
            var moduleIds = getCheckedModules();
            if (moduleIds.length == 0) {
                $("#showConfigTip").text("模块不能为空");
            }
            else {
                var URL = '/superdiamond/module/exportProperties/${type}/${projectId}/' + moduleIds;
                var propertiesData = getProperties(URL);
                exportProperties(propertiesData);
                window.location.href = '/superdiamond/profile/${type}/${projectId}';
            }
        });

        function getProperties(URL) {
            debugger
            var propertiesString = null;
            $.ajax({
                type: "get",
                async: false,
                url: URL,
                dataType: "text",
                success: function (data) {
                    document.location.href = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                    propertiesString =decodeURIComponent(data);
                    //document.location.href = 'redirect:/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
                },
            });
            return propertiesString;
        }

        function exportProperties(propertiesString) {
            var propertiseFormat = [propertiesString]
            var blob = new Blob(propertiseFormat, {type: 'application/plain'});
            var name = document.getElementById("projectName").value.toString();
            saveAs(blob, name + ".properties");
        }
    })
</script>