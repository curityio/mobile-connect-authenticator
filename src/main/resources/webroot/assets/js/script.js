$(document).ready(function () {

    if ($("#operator").attr("class").indexOf("is-error is-error-danger") > -1) {
        openMNOForm();
        if (getParameterValue("#regionValue")) {
            $('#region')
                .val(getParameterValue("#regionValue"));
            populateCountriesDropdown($("#region").val());
        }
        if (getParameterValue("#countryValue")) {
            $('#country')
                .val(getParameterValue("#countryValue"));
            populateOperatorsDropdown($("#country").val(), $("#region").val());
        }

    } else {
        $("#MNOForm").hide();
    }

    $("#mobileNumberForm-link").click(function () {
        $("#MNOForm").hide();
        $("#mobileNumberForm").show("slow");
        hideErrorMessage();
    });

    $("#MNOForm-link").click(function () {
        openMNOForm();
        hideErrorMessage();
    });

    $("#region").change(function () {
        populateCountriesDropdown($("#region").val());
        populateOperatorsDropdown($("#country").val(), $("#region").val());
    });

    $("#country").change(function () {
        populateOperatorsDropdown($("#country").val(), $("#region").val());
    });

    //hide error message on value change
    $("#operator").change(function () {
        hideErrorMessage();
    });

    $("#mobileNumber").change(function () {
        hideErrorMessage();
    });

});

function getParameterValue(name){
    return $(name).text().trim().replace("[","").replace("]","")
}

function openMNOForm() {
    populateCountriesDropdown($("#region").val());
    populateOperatorsDropdown($("#country").val(), $("#region").val());
    $("#mobileNumberForm").hide();
    $("#MNOForm").show("slow");
    $('#mobileNumber').val("");
}

function hideErrorMessage() {
    $("body > main > div > div.mt3.px3.lg-px4 > div").hide()
}

function populateCountriesDropdown(region) {
    var countries = regionCountries[region];
    var countriesLowerCase = regionCountries[region + "_lowercase"];
    $('#country')
        .find('option')
        .remove()
        .end();
    for (var i = 0; i < countries.length; i++) {
        jQuery('<option/>', {
            value: countriesLowerCase[i].split(" ").join("_"),
            html: countries[i]
        }).appendTo('#country');
    }

    $('#country')
        .val(countriesLowerCase[0].split(" ").join("_"));
}

function populateOperatorsDropdown(country, region) {
    $('#operator')
        .find('option')
        .remove()
        .end();

    var operatorsIndex = regionCountries[region + "_lowercase"].indexOf(country.split("_").join(" "));
    if (operatorsIndex > -1 && operators[region]) {
        var operatorsList = operators[region][operatorsIndex];
        for (var i = 0; i < operatorsList.length; i++) {
            jQuery('<option/>', {
                value: operatorsList[i].mcc_mnc,
                html: operatorsList[i].operator
            }).appendTo('#operator');
        }

        $('#operator')
            .val(operatorsList[0].mcc_mnc.split(" ").join("_"));
    } else {
        jQuery('<option/>', {
            value: "",
            html: "Please choose your operator"
        }).appendTo('#operator');
    }
}

var europeCountries = "Albania,Andorra,Armenia,Austria,Azerbaijan,Belarus,Belgium,Bosnia and Herzegovina,Bulgaria,Croatia,Cyprus,Czech Republic,Denmark,Estonia,Faroe Islands,Finland,France,Georgia,Germany,Gibraltar,Greece,Greenland,Guernsey,Hungary,Iceland,Ireland,Isle of Man,Italy,Jersey,Kosovo[a],Latvia,Liechtenstein,Lithuania,Luxembourg,Macedonia,Malta,Moldova,Monaco,Montenegro,Netherlands,Norway,Poland,Portugal,Romania,Russia,San Marino,Serbia,Slovakia,Slovenia,Spain,Sweden,Switzerland,Ukraine,United Kingdom";
var northAmericaCountries = "Anguilla,Antigua and Barbuda,Argentina,Aruba,Bahamas,Barbados,Belize,Bermuda,Bolivia,Bonaire,Brazil,British Virgin Islands,Canada,Cayman Islands,Chile,Colombia,Costa Rica,Cuba,Curaçao,Dominica,Dominican Republic,Ecuador,El Salvador,Falkland Islands,Grenada,Guadeloupe, Martinique and French Guiana (Guyane),Guatemala,Guyana,Haiti,Honduras,Jamaica,Mexico,Montserrat,Nicaragua,Panama,Paraguay,Peru,Puerto Rico & United States Virgin Islands,Saint Kitts and Nevis,Saint Lucia,Saint Vincent and the Grenadines,Suriname,Trinidad and Tobago,Turks and Caicos Islands,United States,Uruguay,Venezuela";
var regionCountries = {
    "europe": europeCountries.split(","),
    "europe_lowercase": europeCountries.toLowerCase().split(","),
    "north_america": northAmericaCountries.split(","),
    "north_america_lowercase": northAmericaCountries.toLowerCase().split(",")
};

var operators = {
    "europe": [[{
        "operator": "Vodafone Albania",
        "mcc_mnc": "27602"
    }, {
        "operator": "Telekom Albania (Formerly AMC)",
        "mcc_mnc": "27601"
    }, {"operator": "Albtelecom (Formerly EAGLE MOBILE", "mcc_mnc": "27603"}, {
        "operator": "Plus",
        "mcc_mnc": "27604"
    }], [{
        "operator": "Andorra Telecom (formerly Servei de Telecomunicacions d'Andorra (STA))",
        "mcc_mnc": "21303"
    }], [{"operator": "VivaCell-MTS", "mcc_mnc": "28305"}, {"operator": "Ucom", "mcc_mnc": "28310"}, {
        "operator": "Beeline",
        "mcc_mnc": "28301"
    }], [{"operator": "A1", "mcc_mnc": "23201"}, {
        "operator": "T-Mobile (Formerly max.mobil.)",
        "mcc_mnc": "23202"
    }, {"operator": "3 •Includes the previous Orange network.", "mcc_mnc": "23205"}], [{
        "operator": "Azercell",
        "mcc_mnc": "40001"
    }, {"operator": "Bakcell", "mcc_mnc": "40002"}, {"operator": "Nar", "mcc_mnc": "40004"}, {
        "operator": "Aztrank",
        "mcc_mnc": "40003"
    }, {"operator": "Nakhtel", "mcc_mnc": "40006"}], [{"operator": "MTS", "mcc_mnc": "25702"}, {
        "operator": "Velcom",
        "mcc_mnc": "25701"
    }, {"operator": "Life:)", "mcc_mnc": "25704"}, {"operator": "beCloud", "mcc_mnc": "25706"}], [{
        "operator": "Proximus",
        "mcc_mnc": "20601"
    }, {"operator": "Orange (formerly Mobistar)", "mcc_mnc": "20610"}, {
        "operator": "Telenet/Base",
        "mcc_mnc": "20620"
    }], [{"operator": "BH Mobile", "mcc_mnc": "21890"}, {"operator": "m:tel", "mcc_mnc": "21805"}, {
        "operator": "HT Eronet",
        "mcc_mnc": "21803"
    }], [{"operator": "Mtel (Formerly Mobiltel)", "mcc_mnc": "28401"}, {
        "operator": "Telenor (Formerly Globul)",
        "mcc_mnc": "28405"
    }, {"operator": "Vivacom (Formerly vivatel)", "mcc_mnc": "28403"}, {
        "operator": "MAX (Formerly Max Telecom)",
        "mcc_mnc": "28413"
    }, {"operator": "Bulsatcom", "mcc_mnc": "28411"}], [{
        "operator": "Hrvatski Telekom (Formerly T-Mobile, HTmobile)",
        "mcc_mnc": "21901"
    }, {"operator": "Vip", "mcc_mnc": "21910"}, {"operator": "Tele2", "mcc_mnc": "21902"}], [{
        "operator": "MTN",
        "mcc_mnc": "28010"
    }, {"operator": "CYTA", "mcc_mnc": "28001"}, {
        "operator": "PrimeTel",
        "mcc_mnc": "28020"
    }], [{"operator": "T-Mobile (Formerly Paegas)", "mcc_mnc": "23001"}, {
        "operator": "O2 (Formerly Eurotel)",
        "mcc_mnc": "23002"
    }, {"operator": "Vodafone (Formerly Oskar)", "mcc_mnc": "23003"}], [{
        "operator": "TDC",
        "mcc_mnc": "23801"
    }, {"operator": "Telenor (Formerly Sonofon)", "mcc_mnc": "23802"}, {
        "operator": "Telia",
        "mcc_mnc": "23820"
    }, {"operator": "Hutchison 3", "mcc_mnc": "23806"}, {"operator": "Net 1", "mcc_mnc": ""}], [{
        "operator": "Telia",
        "mcc_mnc": "24801"
    }, {"operator": "Elisa (Formerly Radiolinja)", "mcc_mnc": "24802"}, {
        "operator": "Tele2",
        "mcc_mnc": "24803"
    }], [{"operator": "Føroya Tele", "mcc_mnc": "28801"}, {
        "operator": "Vodafone (Formerly Kall)",
        "mcc_mnc": "28802"
    }], [{"operator": "Telia", "mcc_mnc": "24436"}, {"operator": "Elisa", "mcc_mnc": "24405"}, {
        "operator": "DNA",
        "mcc_mnc": "24403"
    }, {"operator": "Ålcom (Formerly ÅMT / Ålands Mobiltelefon)", "mcc_mnc": "24414"}, {
        "operator": "Ukko Mobile",
        "mcc_mnc": "24431"
    }], [{"operator": "Orange (Formerly Itineris)", "mcc_mnc": "20801"}, {
        "operator": "SFR",
        "mcc_mnc": "20810"
    }, {"operator": "Bouygues Telecom", "mcc_mnc": "20820"}, {
        "operator": "Free Mobile",
        "mcc_mnc": "20815"
    }], [{"operator": "MagtiCom", "mcc_mnc": "28202"}, {"operator": "Geocell", "mcc_mnc": "28201"}, {
        "operator": "Beeline",
        "mcc_mnc": "28204"
    }, {"operator": "Silknet", "mcc_mnc": "28205"}], [{
        "operator": "Vodafone (Formerly D2 Mannesmann)",
        "mcc_mnc": "26202"
    }, {
        "operator": "O2 (Formerly Viag Interkom)",
        "mcc_mnc": "26203"
    }, {"operator": "Telekom (Formerly D1 DeTeMobil, T-Mobile)", "mcc_mnc": "26201"}], [{
        "operator": "GIBTEL",
        "mcc_mnc": "26601"
    }], [{"operator": "COSMOTE", "mcc_mnc": "NO"}, {
        "operator": "Vodafone (Formerly Panafon)",
        "mcc_mnc": "NO"
    }, {"operator": "WIND (Formerly Q-Telecom)", "mcc_mnc": "NO"}, {
        "operator": "WIND (Formerly Telestet, TIM)",
        "mcc_mnc": "NO"
    }, {"operator": "CYTA", "mcc_mnc": "YES (using vodafone's network, 4G not allowed)"}], [{
        "operator": "TELE Greenland",
        "mcc_mnc": "29001"
    }], [{"operator": "Sure Mobile", "mcc_mnc": "23455"}, {
        "operator": "JT (Guernsey) Global",
        "mcc_mnc": "23450"
    }, {"operator": "Airtel-Vodafone", "mcc_mnc": "23403"}], [{
        "operator": "Telekom (Formerly Westel, T-Mobile)",
        "mcc_mnc": "21630"
    }, {"operator": "Telenor (Formerly Pannon)", "mcc_mnc": "21601"}, {
        "operator": "Vodafone",
        "mcc_mnc": "21670"
    }, {"operator": "Digi.Mobil (testing)", "mcc_mnc": "21603"}], [{
        "operator": "Síminn",
        "mcc_mnc": "27401"
    }, {"operator": "Vodafone", "mcc_mnc": "27402"}, {"operator": "Nova", "mcc_mnc": "27411"}, {
        "operator": "IceCell",
        "mcc_mnc": "27407"
    }], [{
        "operator": "Vodafone (Formerly Eircell)",
        "mcc_mnc": "27201"
    }, {
        "operator": "Three •Includes the previous O2 network.",
        "mcc_mnc": "27205 (Three) 27202 (O2)"
    }, {"operator": "Eir Mobile (Formerly Meteor)  ", "mcc_mnc": "27203"}], [{
        "operator": "Manx Telecom",
        "mcc_mnc": "23458"
    }, {"operator": "Sure Mobile", "mcc_mnc": "23436"}], [{
        "operator": "Wind Tre",
        "mcc_mnc": "22288 (Wind) 22299 (Tre)  "
    }, {"operator": "TIM", "mcc_mnc": "22201"}, {
        "operator": "Vodafone (Formerly Omnitel)",
        "mcc_mnc": "22210"
    }], [{"operator": "JT (Jersey) Global", "mcc_mnc": "23450"}, {
        "operator": "Sure Mobile",
        "mcc_mnc": "23455"
    }, {"operator": "Airtel-Vodafone", "mcc_mnc": "23403"}], [{
        "operator": "Vala",
        "mcc_mnc": "22101"
    }, {"operator": "IPKO Telecommunications LLC", "mcc_mnc": "22102"}, {
        "operator": "Z mobile",
        "mcc_mnc": "22106"
    }, {"operator": "mts DOO", "mcc_mnc": "22103"}, {"operator": "D3 mobile", "mcc_mnc": "22102"}], [{
        "operator": "lmt",
        "mcc_mnc": "24701"
    }, {"operator": "Tele2", "mcc_mnc": "24702"}, {"operator": "Bite", "mcc_mnc": "24705"}, {
        "operator": "Triatel",
        "mcc_mnc": "24703"
    }], [{
        "operator": "Telecom Liechtenstein AG (formerly Mobilkom Liechtenstein AG)",
        "mcc_mnc": "29505"
    }, {
        "operator": "Salt (Liechtenstein) AG (formerly Orange (Liechtenstein) AG)",
        "mcc_mnc": "29502"
    }, {"operator": "Swisscom (Schweiz) AG", "mcc_mnc": "29501"}], [{
        "operator": "Telia",
        "mcc_mnc": "24601"
    }, {"operator": "BITĖ", "mcc_mnc": "24602"}, {
        "operator": "Tele2",
        "mcc_mnc": "24603"
    }, {"operator": "Lietuvos radijo ir televizijos centras (MEZON)", "mcc_mnc": "24608"}], [{
        "operator": "POST",
        "mcc_mnc": "27001"
    }, {"operator": "Tango", "mcc_mnc": "27077"}, {"operator": "Orange", "mcc_mnc": "27099"}, {
        "operator": "LOL Mobile",
        "mcc_mnc": ""
    }], [{"operator": "Vip", "mcc_mnc": "29403"}, {
        "operator": "Telekom (Formerly T-Mobile, MOBIMAK)",
        "mcc_mnc": "29401"
    }], [{"operator": "Vodafone Malta (Formerly Telecell)", "mcc_mnc": "27801"}, {
        "operator": "GO",
        "mcc_mnc": "27821"
    }, {"operator": "Melita", "mcc_mnc": "27877"}], [{"operator": "Orange", "mcc_mnc": "25901"}, {
        "operator": "Moldcell",
        "mcc_mnc": "25902"
    }, {"operator": "Unité", "mcc_mnc": "25905"}, {
        "operator": "Interdnestrcom operating in Transnistria region only",
        "mcc_mnc": "25903"
    }], [{"operator": "Monaco Telecom", "mcc_mnc": "21201"}], [{
        "operator": "Telenor Formerly Promonte",
        "mcc_mnc": "29701"
    }, {"operator": "Telekom Formerly Monet", "mcc_mnc": "29702"}, {
        "operator": "m:tel",
        "mcc_mnc": "29703"
    }], [{
        "operator": "KPN •Includes the previous Telfort network.",
        "mcc_mnc": "20408"
    }, {
        "operator": "Vodafone (Formerly Libertel)",
        "mcc_mnc": "20404"
    }, {
        "operator": "T-Mobile (Formerly Ben) •Includes the previous Orange network.  ",
        "mcc_mnc": "20416"
    }, {"operator": "Tele2 ", "mcc_mnc": "20402"}], [{
        "operator": "Telenor",
        "mcc_mnc": "24201"
    }, {
        "operator": "Telia (Formerly NetCom) •Includes the previous Tele2 network.",
        "mcc_mnc": "24202"
    }, {"operator": "Ice.net", "mcc_mnc": "24214, 24206"}], [{
        "operator": "Play",
        "mcc_mnc": "26006"
    }, {"operator": "Orange (Formerly IDEA)", "mcc_mnc": "26003"}, {
        "operator": "Plus (Formerly Plus GSM)",
        "mcc_mnc": "26001"
    }, {"operator": "T-Mobile (Formerly Era)", "mcc_mnc": "26002"}], [{
        "operator": "MEO (Formerly TMN)",
        "mcc_mnc": "26806"
    }, {"operator": "Vodafone (Formerly Telecel)", "mcc_mnc": "26801"}, {
        "operator": "NOS (Formerly Optimus)",
        "mcc_mnc": "26803"
    }], [{"operator": "Orange (Formerly Dialog)", "mcc_mnc": "22610"}, {
        "operator": "Vodafone (Formerly Connex)",
        "mcc_mnc": "22601"
    }, {"operator": "Telekom (Formerly Cosmorom, Cosmote)", "mcc_mnc": "22603"}, {
        "operator": "Digi.Mobil",
        "mcc_mnc": "22605"
    }], [{"operator": "MTS", "mcc_mnc": "25001"}, {"operator": "MegaFon", "mcc_mnc": "25002"}, {
        "operator": "Beeline",
        "mcc_mnc": "25099"
    }, {"operator": "Tele2", "mcc_mnc": "25020"}, {
        "operator": "Sotovaja Svjaz MOTIV",
        "mcc_mnc": "25035"
    }, {"operator": "SMARTS", "mcc_mnc": "25007"}, {
        "operator": "Tattelecom",
        "mcc_mnc": "25054"
    }, {"operator": "Sotovaja Svjaz Bashkortostana (SSB)", "mcc_mnc": "25009"}, {
        "operator": "Vainahtelecom",
        "mcc_mnc": "25022"
    }], [{"operator": "TMS", "mcc_mnc": ""}, {"operator": "TIM San Marino", "mcc_mnc": ""}, {
        "operator": "Prima",
        "mcc_mnc": "29201"
    }], [{"operator": "mts", "mcc_mnc": "22003"}, {"operator": "Telenor", "mcc_mnc": "22001"}, {
        "operator": "vip",
        "mcc_mnc": "22005"
    }], [{
        "operator": "Orange (Formerly Globtel,Slovtel)",
        "mcc_mnc": "23101"
    }, {"operator": "Telekom (Formerly Eurotel, T-Mobile)", "mcc_mnc": "23102"}, {
        "operator": "O2",
        "mcc_mnc": "23106"
    }, {"operator": "4KA / SWAN Mobile", "mcc_mnc": "23103"}], [{
        "operator": "Telekom Slovenije (formerly Mobitel)",
        "mcc_mnc": "29341"
    }, {"operator": "A1 Slovenija (formerly Si.mobil)", "mcc_mnc": "29340"}, {
        "operator": "Telemach (formerly Tušmobil)",
        "mcc_mnc": "29370"
    }, {"operator": "T-2", "mcc_mnc": "29364"}], [{"operator": "Movistar", "mcc_mnc": "21407"}, {
        "operator": "Orange",
        "mcc_mnc": "21403"
    }, {"operator": "Vodafone ", "mcc_mnc": "21401"}, {"operator": "Yoigo", "mcc_mnc": "21404"}], [{
        "operator": "Telia",
        "mcc_mnc": "24001"
    }, {"operator": "Tele2 ", "mcc_mnc": "24007"}, {
        "operator": "Telenor (Formerly Vodafone)",
        "mcc_mnc": "24006"
    }, {"operator": "Tre", "mcc_mnc": "24002"}], [{
        "operator": "Swisscom",
        "mcc_mnc": "22801"
    }, {"operator": "Sunrise (Formerly diAx)", "mcc_mnc": "22802"}, {
        "operator": "Salt (Formerly Orange)",
        "mcc_mnc": "22803"
    }], [{"operator": "Kyivstar", "mcc_mnc": "25503"}, {
        "operator": "Vodafone (Formerly MTS Ukraine)",
        "mcc_mnc": "25501"
    }, {"operator": "lifecell (Formerly life:))", "mcc_mnc": "25506"}, {
        "operator": "Intertelecom",
        "mcc_mnc": "25504"
    }, {"operator": "TriMob", "mcc_mnc": "25507"}, {"operator": "PEOPLEnet", "mcc_mnc": "25521"}], [{
        "operator": "EE",
        "mcc_mnc": "23430 and 23433"
    }, {"operator": "O2", "mcc_mnc": "23410"}, {"operator": "Vodafone", "mcc_mnc": "23415"}, {
        "operator": "Three",
        "mcc_mnc": "23420"
    }]]
};

