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

    $('[data-metrics]').each(function() {
        var metrics = $(this).attr('data-metrics');
        var parts = metrics.split(':');
        ga('send', 'event', parts[0], parts[1], parts[2]);
    });

    $( "a[target='_blank']" ).attr("data-journey-click", "link - click:UR banner:Participate");

    function setCookie (name, value, duration, domain) {
        var secure = window.location.protocol.indexOf('https') ? '' : '; secure'

        var cookieDomain = ""
        if (domain) {
            cookieDomain = '; domain=' + domain
        }

        var expires = ""
        if (duration) {
            var date = new Date()
            date.setTime(date.getTime() + (duration * 24 * 60 * 60 * 1000))
            expires = '; expires=' + date.toGMTString()
        }

        document.cookie = name + '=' + value + expires + cookieDomain + '; path=/' + secure
    }

    var cookieData=GOVUK.getCookie("mdtpurr");
    if (cookieData == null) {
        $("#full-width-banner").addClass("banner--show");
    } else {
        $("#full-width-banner").removeClass("banner--show");
    }

    var bannerClose = $(".full-width-banner__close")

    bannerClose.on('click', function(e){
        e.preventDefault();
        GOVUK.setCookie("mdtpurr", "suppress_for_all_services", 99999999999);
        setCookie("mdtpurr", "suppress_for_all_services", 28);
        $("#full-width-banner").removeClass("banner--show");
        ga('send', 'event', 'link - click', 'UR banner', 'Dismiss');
    });


}));


window.onload = function() {

    var errors = document.getElementsByClassName("js-error-summary-messages");
    if(errors.length > 0){
        var err = errors[0].getElementsByTagName("a");
        for(i = 0; i < err.length; i++){
            errorToGA(err[i], i);
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
