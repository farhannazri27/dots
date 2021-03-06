<%@page contentType="text/html;charset=UTF-8" %>
<%@include file="/WEB-INF/base/taglibs.jsp" %>
<s:layout-render name="/WEB-INF/base/base.jsp">
    <s:layout-component name="page_css">
        <link rel="stylesheet" href="${contextPath}/resources/private/css/libs/bootstrap-select.css" type="text/css" />
        <link rel="stylesheet" href="${contextPath}/resources/private/css/libs/select2.css" type="text/css" />
        <link rel="stylesheet" href="${contextPath}/resources/private/css/libs/datepicker.css" type="text/css" />
    </s:layout-component>
    <s:layout-component name="page_css_inline">
    </s:layout-component>
    <s:layout-component name="page_container">
        <div class="col-lg-12">
            <h1>New Interval</h1>
            <div class="row">
                <div class="col-lg-8">
                    <div class="main-box">
                        <h2>RMS#_Event Information</h2>
                        <form id="add_hardwarequest_form" class="form-horizontal" role="form" action="${contextPath}/lotPenang/newIntervalSave" method="post">
                            <div class="form-group">
                                  <input type="hidden" name="oldLotPenangId" id ="oldLotPenangId" value="${lotPenang.id}" />
                                <label for="barcode" class="col-lg-2 control-label">RMS#_Event</label>
                                <div class="col-lg-4">
                                    <input type="text" class="form-control" id="barcode" name="barcode" value="${lotPenang.rmsEvent}" readonly>
                                </div>
                            </div>
                            <div class="form-group">
                                <label for="interval" class="col-lg-2 control-label">Interval</label>
                                <div class="col-lg-3">
                                    <input type="text" class="form-control" id="interval" name="interval" value="" autofocus>
                                </div>
                            </div>
                            <a href="${contextPath}/lotPenang" class="btn btn-info pull-left"><i class="fa fa-reply"></i> Back</a>
                            <div class="pull-right">
                                <button type="reset" class="btn btn-secondary cancel">Reset</button>
                                <button type="submit" id="submit" class="btn btn-primary">Submit</button>
                            </div>
                            <div class="clearfix"></div>
                        </form>
                    </div>
                </div>	
            </div>
        </div>
    </s:layout-component>
    <s:layout-component name="page_js">
        <script src="${contextPath}/resources/private/js/select2.min.js"></script>
        <script src="${contextPath}/resources/private/js/bootstrap-select.js"></script>
        <script src="${contextPath}/resources/validation/jquery.validate.min.js"></script>
        <script src="${contextPath}/resources/validation/additional-methods.js"></script>
        <script src="${contextPath}/resources/private/js/bootstrap-datepicker.js"></script>
    </s:layout-component>
    <s:layout-component name="page_js_inline">
        <script>
            $(document).ready(function () {


                var validator = $("#add_hardwarequest_form").validate({
                    rules: {
                        interval: {
                            required: true,
                            number: true
                        }
                    }
                });
                $(".cancel").click(function () {
                    validator.resetForm();
                });
            });

        </script>
    </s:layout-component>
</s:layout-render>