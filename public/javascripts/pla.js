$(document).ready($(function() {

    $('*[data-hidden]').each(function() {

        var $self = $(this);
        var $hidden = $('#hidden')
        var $input = $self.find('input');

        if ($input.val() === 'yes' && $input.prop('checked')) {
            $hidden.show();
        } else {
            $hidden.hide();
        }

        $input.change(function() {

            var $this = $(this);

            if ($this.val() === 'yes') {
                $hidden.show();
            } else if($this.val() === 'no') {
                $hidden.hide();
            }
        });
    });

    var radioOptions = $('input[type="radio"]');

    radioOptions.each(function() {
        var o = $(this).parent().next('.additional-option-block');
        if ($(this).prop('checked')) {
            o.show();
        } else {
            o.hide();
        }
    });

    radioOptions.on('click', function(e){
        var o = $(this).parent().next('.additional-option-block');
        if(o.index() == 1){
            $('.additional-option-block').hide();
            o.show();
        }
    });
}));

var ip16SubmitToGA = function() {
    var idArray = ["pensionsTakenDisplayValue0",
                "pensionsTakenBeforeDisplayValue0",
                "pensionsTakenBetweenDisplayValue0",
                "overseasPensionsDisplayValue0"];
    for(i = 0; i < idArray.length; i++){
        if(document.getElementById(idArray[i]) == null){

        } else if(document.getElementById(idArray[i]).innerHTML == "Yes"){
            ga('send', 'event', 'submitSummary', idArray[i].replace("DisplayValue0",""), 'yes');
        } else if(document.getElementById(idArray[i]).innerHTML == "No"){
            ga('send', 'event', 'submitSummary', idArray[i].replace("DisplayValue0",""), 'no');
        }
    }
};

var ip14SubmitToGA = function() {
    var idArray = ["ip14PensionsTakenDisplayValue0",
                "ip14PensionsTakenBeforeDisplayValue0",
                "ip14PensionsTakenBetweenDisplayValue0",
                "ip14OverseasPensionsDisplayValue0"];
    for(i = 0; i < idArray.length; i++){
        if(document.getElementById(idArray[i]) == null){

        } else if(document.getElementById(idArray[i]).innerHTML == "Yes"){
            ga('send', 'event', 'ip14SubmitSummary', idArray[i].replace("DisplayValue0",""), 'yes');
        } else if(document.getElementById(idArray[i]).innerHTML == "No"){
            ga('send', 'event', 'ip14SubmitSummary', idArray[i].replace("DisplayValue0",""), 'no');
        }
    }
};

window.onload = function() {


    var errors = document.getElementsByClassName("js-error-summary-messages");
    if(errors.length > 0){
        var err = errors[0].getElementsByTagName("a");
        for(i = 0; i < err.length; i++){
            errorToGA(err[i], i);
        }
    }

    var errHeading = document.getElementById("error-summary-heading");
    if(errHeading != null) {
        if(stringContains(errHeading.innerHTML, "problem with your application")) {
            ga('send', 'event', "error-Relative-Amount", "summary", "insufficient");
        }
    }

    function errorToGA(err, index) {
        var errFamily = getErrorFamily(err.id);
        var errPage = getErrorPage(err.id);
        var errType = getErrorType(err.innerHTML);
        ga('send', 'event', errFamily, errPage, errType);
    }

    function getErrorFamily(errorId) {
        if(stringContains(errorId, "Amt")) {
            return "error-Amount";
        } else if (dateError(errorId)) {
            return "error-Date";
        } else {
            return "error-Radio";
        }
    }

    function getErrorPage(errorId) {
        if (stringContains(errorId, "pensionsTakenBefore")) return "pensionsTakenBefore";
        if (stringContains(errorId, "pensionsTakenBetween")) return "pensionsTakenBetween";
        if (stringContains(errorId, "pensionsTaken")) return "pensionsTaken";
        if (stringContains(errorId, "overseasPensions")) return "overseasPensions";
        if (stringContains(errorId, "currentPensions")) return "currentPensions";
        if (stringContains(errorId, "pensionDebits")) return "pensionDebits";
        if (stringContains(errorId, "numberOfPSOs")) return "numberOfPSOs";
        if (dateError(errorId) || stringContains(errorId, "psoAmt")) return "psoDetails";
        else return "unknownPage";
    }

    function getErrorType(errMsg) {
        if(stringContains(errMsg, "0 or more")) return "negativeAmount";
        if(stringContains(errMsg, "less than")) return "amountOutOfRange";
        if(stringContains(errMsg, "Enter a date after")) return "dateOutOfRange";
        if(stringContains(errMsg, "without commas")) return "invalidFormat";
        if(stringContains(errMsg, "date in the correct format")) return "invalidFormat";
        if(stringContains(errMsg, "decimal places")) return "decimalPlaces";
        else return "mandatory";
    };

    function dateError(errorId) {
        return stringContains(errorId, "Day") || stringContains(errorId, "Month") || stringContains(errorId, "Year");
    }

    function stringContains(str, cont) {
        return str.indexOf(cont) != -1;
    }
};
