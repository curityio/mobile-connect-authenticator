$(document).ready(function () {
    $("#MNOForm").hide("slow");

    $("#mobileNumberForm-link").click(function () {
        $("#MNOForm").hide("slow");
        $("#mobileNumberForm").show("slow");
    });

    $("#MNOForm-link").click(function () {
        $("#mobileNumberForm").hide("slow");
        $("#MNOForm").show("slow");
    });
});
