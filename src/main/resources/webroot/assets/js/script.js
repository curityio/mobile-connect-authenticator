$(document).ready(function () {

    $("#MNOForm").hide();

    $("#mobileNumberForm-link").click(function () {
        $("#MNOForm").hide();
        $("#mobileNumberForm").show("slow");

    });

    $("#MNOForm-link").click(function () {
        $("#mobileNumberForm").hide();
        $("#MNOForm").show("slow");
    });
});
