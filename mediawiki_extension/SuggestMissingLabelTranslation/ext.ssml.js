(function( $, mw ) { $( document ).ready( function() {
    selectPages( false );

    /** Select/Unselect all checkboxes from Nuke Mediawiki extension. **/
    function selectPages( check ) {
        $( 'input[type=checkbox]' ).prop( 'checked', check )
    }

    $( '#toggleall' ).click( function(){
        selectPages( true );
    });

    $( '#togglenone' ).click( function(){
        selectPages( false );
    });

    $( 'form#approve' ).submit( function( event ){
        var hidden_lang = $(this).find('input[name=language]')

        $('tr').each(function(){
           var checked = $(this).find('input[type=checkbox]').prop('checked');
           if(!checked) {
               return true; // continue
           }

           var row_data = []
           row_data.push(hidden_lang.serialize());
           row_data.push($(this).find('td input').serialize());
           row_data.push($(this).find('input[name=title]').serialize());

           sendApprove(row_data);
           $(this).remove();
        })

        event.preventDefault();
    });

    function sendApprove(form_data) {
        var cookie_prefix = mw.config.get('wgCookiePrefix');

        form_data.push("session_prefix=" + cookie_prefix);
        form_data.push("session_id=" + $.cookie('sid'));
        form_data.push("token=" + encodeURIComponent(mw.user.tokens.get('editToken')));

        console.log(form_data)
        $.ajax({
            type: "POST",
            url: mw.config.get('rest_url') + "approve",
            data: form_data.join('&'),
            success: function() {
                         console.log("success")
                     },
            dataType: "json",
            async: false
        });
    }

} ); })( window.jQuery, window.mediaWiki );
