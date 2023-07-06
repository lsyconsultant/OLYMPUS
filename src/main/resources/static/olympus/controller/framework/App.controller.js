sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel",
], function (Controller, MessageToast, JSONModel) {
    "use strict";

    return Controller.extend("sap.ui.olympus.controller.framework.App", {

        onInit: function () {
            // debugger;
            var oData = {
                loginStatus: this.getOwnerComponent().getModel().getProperty("/loginStatus")
            };
            var oModel = new JSONModel(oData);
            this.getView().setModel(oModel);
            console.log(this.getView().getModel().getProperty("/memberId"))
        },
        onItemSelect: function (event) {
            const selectedItem = event.getParameter("item");
            const routeName = selectedItem.getKey();
            this.getOwnerComponent().getRouter().navTo(routeName, {});
        },
        onLogin() {
            var that = this;
            $.ajax({
                type: "post",
                url: "/login/login.do",
                data: {
                    id: this.getView().getModel().getProperty("/id"),
                    password: this.getView().getModel().getProperty("/password")
                },
                cache: false,
                async: false,
                dataType: "json",
                success: function (result) {

                    MessageToast.show(result.resultMsg);
                    if (result.resultCd === 'true') {
                        sap.m.URLHelper.redirect("/", false);
                    }

                },
                error: function (data) {
                    console.log(data.error().responseText);
                }
            });
        },
        onLogout() {
            var that = this;
            $.ajax({
                type: "post",
                url: "/login/loginout.do",
                data: {},
                cache: false,
                async: false,
                dataType: "json",
                success: function (result) {

                    MessageToast.show(result.resultMsg);
                    if (result.resultCd === 'true') {
                        sap.m.URLHelper.redirect("/", false);
                    }

                },
                error: function (data) {
                    console.log(data.error().responseText);
                }
            });
        }

    });

});
