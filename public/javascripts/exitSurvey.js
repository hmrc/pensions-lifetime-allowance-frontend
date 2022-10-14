$(document).ready($(function() {

    $('*[data-hidden]').each(function() {

        var $self = $(this);
        var $hidden = $('#hidden')
        var $input = $self.find('input');

        if ($input.val() === ('yes' || 'no') && $input.prop('checked')) {
            $hidden.show();
        } else {
            $hidden.hide();
        }

        $input.change(function() {

            var $this = $(this);

            if ($this.val() === 'yes' || 'no') {
                $hidden.show();
            } else {
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


var submitExitSurvey = function() {
    var eSRadioArray = ["phoneOrWrite-yes",
                        "phoneOrWrite-no",
                        "phoneOrWriteNow-yes",
                        "phoneOrWriteNow-no",
                        "phoneOrWriteNow-don't_know",
                        "recommend-very_likely",
                        "recommend-likely",
                        "recommend-not_likely_or_unlikely",
                        "recommend-unlikely",
                        "recommend-very_unlikely",
                        "satisfaction-very_satisfied",
                        "satisfaction-satisfied",
                        "satisfaction-not_satisfied_or_dissatisfied",
                        "satisfaction-dissatisfied",
                        "satisfaction-very_dissatisfied"];
    for(i = 0; i < eSRadioArray.length; i++){
        if(document.getElementById(eSRadioArray[i]).checked){
        }
    }

    var eSCheckArray = ["anythingElse-nothing_else",
                        "anythingElse-online_help",
                        "anythingElse-employer_help",
                        "anythingElse-family_help",
                        "anythingElse-agent_help",
                        "anythingElse-may_need_help",
                        "anythingElse-something_else",]
    for(i = 0; i < eSCheckArray.length; i++){
        if(document.getElementById(eSCheckArray[i]).checked){
        }
    }
};
