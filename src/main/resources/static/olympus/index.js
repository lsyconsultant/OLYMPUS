sap.ui.define([
    "sap/ui/core/ComponentContainer"
], function(ComponentContainer) {
    "use strict";

    new ComponentContainer({
        name: "sap.ui.olympus",
        settings : {
            id : "olympus"
        },
        async: true
    }).placeAt("content");

});