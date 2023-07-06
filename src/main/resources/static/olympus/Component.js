sap.ui.define([
    "sap/ui/core/UIComponent",
    "sap/ui/model/json/JSONModel",
    "sap/ui/model/resource/ResourceModel"
], function (UIComponent, JSONModel, ResourceModel) {
    "use strict";

    return UIComponent.extend("sap.ui.olympus.Component", {

        metadata: {
            interfaces: ["sap.ui.core.IAsyncContentCreation"],
            manifest: "json",
        },
        init: function () {
            var oData = {};
            var oModel = new JSONModel(oData);
            this.setModel(oModel);

            // call the init function of the parent
            UIComponent.prototype.init.apply(this, arguments);

            // set i18n model
            var i18nModel = new ResourceModel({
                bundleName: "sap.ui.olympus.i18n.i18n"
            });
            this.setModel(i18nModel, "i18n");

            //로그인 정보 조회
            this.getLoginStatus()

            // create the views based on the url/hash
            this.getRouter().initialize();

        },
        getLoginStatus() {
            console.log("로그인 상태 확인");

            var that = this;
            var user = "";
            $.ajax({
                type: "get",
                url: "/login/loginStatus.do",
                data: {},
                cache: false,
                async: false,
                dataType: "json",
                success: function (result) {

                    user = result;
                    //manifest에 Routing 정보를 등록하지 않고, 아래 부분에 등록하도록 한다.
                    //서버에서 로그인 유저의 로그인 정보를 호출하여 결과를 받아서 Routing을 설정하도록 구성한다.
                    //추후 작업 시작되면, manifest.json 에 routes, targets 두개를 비우고 아래 로직으로 대체한다.
                    // that.getRouter().getTargets().addTarget("dashboard",{
                    //     id:"dashboard",
                    //     name:"dashboard.dashboard"
                    // })
                    //
                    // that.getRouter().addRoute({
                    //     name :"dashboard",
                    //     pattern: "dashboard",
                    //     target: "dashboard"
                    // })

                },
                error: function (data) {
                    console.log(data.error().responseText);
                }
            });

            if(user.data.memberId === "" ){
                var routeName = "login";
                var loginStatus = false;
            }else{
                var routeName = "home";
                var loginStatus = true;
            }
            this.getModel().setProperty("/loginStatus", loginStatus)
            this.getModel().setProperty("/memberId", user.data.memberId)
            this.getRouter().navTo(routeName, {}, true);
            console.log("routeName => " + routeName)
        }
    });

});