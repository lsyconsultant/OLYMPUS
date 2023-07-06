sap.ui.define([
    "sap/ui/core/mvc/Controller",
    "sap/m/MessageToast",
    "sap/ui/model/json/JSONModel",
], function (Controller, MessageToast, JSONModel) {
    "use strict";

    return Controller.extend("sap.ui.olympus.controller.bcAmtMerge.bcAmtMerge", {

        onInit: function () {
            var oData = {
                baseFile: "",
                OKR: "",
                OAZ: "",
                OHC: "",
                OML: "",
                OMSI: "",
                OSP: "",
                OTH: "",
                OVN: "",
                folderPath: ""
            };
            var oModel = new JSONModel(oData);
            this.getView().setModel(oModel);
            console.log(this.getView().getModel().getProperty("/memberId"))

            this.getOwnerComponent().getRouter().attachRouteMatched(function (oEvent) {
                var routeName = oEvent.getParameter("name");
                // this.getView().getModel().setProperty("/currentRoute", routeName);
                // this.getView().getModel().setProperty("/currentRouteArguments", oEvent.getParameter("arguments"));

                if (routeName === "bcAmtMerge") {

                }

            }.bind(this));


            this.sessionSetWorkingFilePath();


        },
        sessionSetWorkingFilePath() {
            var that = this;
            $.ajax({
                type: "get",
                url: "/sessionSetWorkingFilePathMerge.do",
                data: {},
                cache: false,
                async: true,
                success: function (result) {
                    that.getView().getModel().setProperty("/folderPath", result);
                    console.log("작업폴더 : " + that.getView().getModel().getProperty("/folderPath"));
                },
                error: function (data) {
                    console.log(data);
                }
            });
        },
        onMergeStart(event) {
            // var fileList = {
            //     baseFile : "",
            //     OKR : "C:\\olympus\\20230628-093007\\202305_OKR_GIET_sales.xlsx",
            //     // OAZ : "C:\\olympus\\20230628-093007\\202305_OAZ_GIET_sales.xlsx",
            //     // OHC : "C:\\olympus\\20230628-093007\\202305_OHC_GIET_sales.xlsx",
            //     // OML : "C:\\olympus\\20230628-093007\\202305_OML_GIET_sales.xlsx",
            //     // OMSI : "C:\\olympus\\20230628-093007\\202305_OMSI_GIET_sales.xlsx",
            //     // OSP : "C:\\olympus\\20230628-093007\\202305_OSP_GIET_sales.xlsx",
            //     // OTH : "C:\\olympus\\20230628-093007\\202305_OTH_GIET_sales.xlsx",
            //     // OVN : "C:\\olympus\\20230628-093007\\202305_OVN_GIET_sales.xlsx"
            // };
            var fileList = this.getView().getModel().getProperty("/");
            console.log(fileList);

            if(fileList['OKR'] === ""){
                MessageToast.show("한국_OKR 매출 데이터를 필수로 넣어야 합니다.");
                return;
            }
            for (const item in fileList) {
                if(fileList[item] === '가져오는 중....'){
                    MessageToast.show("병합할 데이터가 준비가 완료되지 않았습니다. 잠시 뒤에 다시 진행해주세요.")
                    return;
                }
            }

            this.getView().byId("masterFileLink").setValue("작업중입니다.");
            this.getView().byId("masterFileLink").setBusy(true);
            var that = this;
            $.ajax({
                type: "post",
                url: "/bcAmtMerge.do",
                data: JSON.stringify(fileList),
                cache: false,
                async: true,
                headers: {
                    "Content-Type": "application/json"
                },
                success: function (result) {
                    that.getView().byId("masterFileLink").setValue(result);
                    that.getView().byId("masterFileLink").setBusy(false);
                },
                error: function (data) {
                    console.log(data);
                }
            });
        },
        handleUploadComplete(event) {
            this.getView().getModel().setProperty(
                "/" + event.getParameter("id").split("--")[2],
                event.getParameter("responseRaw")
            );
            this.getView().byId(event.getParameter("id").split("--")[2] + "Label").setBusy(false);
            console.log(this.getView().getModel().getProperty("/"));
        },
        handleUploadProgress(event){
            this.getView().byId(event.getParameter("id").split("--")[2] + "Label").setText("가져오는 중....");
            this.getView().byId(event.getParameter("id").split("--")[2] + "Label").setBusy(true);
        }


    });

});
