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
        console.log("sending: " + parts[0] + " - " + parts[1] + " - " + parts[2])
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
