<?php
class SpecialSuggestMissingLabelTranslation extends SpecialPage {
    public function __construct() {
        parent::__construct( 'SuggestMissingLabelTranslation', 'smlt' );
    }

    var $rest_url = SMLT_REST_URL;
    var $base_url = null;

    function execute( $par ) {
         if( !$this->userCanExecute( $this->getUser() ) ) {
             $this->displayRestrictionError();
             return;
         }

        $this->base_url = $this->getTitle()->getLocalURL();

        $request = $this->getRequest();
        $output = $this->getOutput();
        $this->setHeaders();

        // same cookie as wiki_session but js readable
        setCookie($cookieprefix . "sid", session_id());

        $output->addModules( 'ext.ssml' );

        # use rest url in js extension
        $output->addJsConfigVars("rest_url", $this->rest_url);

        # get request data
        $lang = trim( $request->getText( 'lang'));
        $limit = intval(trim( $request->getText( 'limit', $par)));
        $offset = intval(trim( $request->getText( 'offset', $par)));

        if ( $limit == '' ) {
            $limit = 10;
        }

        if ( $lang == '' ) {
            $lang = "de";
        }

        if ( $offset == '' ) {
            $offset = 0;
        }

        # show filter form
        $this->show_filter( $lang, $limit, $offset);

        $output->addWikiText( "== Translations ==" );

        $missing = $this->get_missing($lang);

        $this->show_table($missing, $lang, $limit, $offset);

        $this->show_pagination( $limit, $offset, $lang, count($missing) );

    }

    protected function get_missing($lang) {
        $jsonurl =  $this->rest_url . $lang;
        $json = file_get_contents($jsonurl);
        $missing = json_decode($json, true);

        return $missing['missing'];
    }

    protected function show_filter($lang, $limit, $offset) {
        # TODO all languages
        $formDescriptor['select'] = array(
                'section' => 'Settings',
                'type' => 'select',
                'name' => 'lang',
                'required' => false,
                'label' => 'Select your language',
                'options' => array(
                    'ar' => 'ar',
                    'be' => 'be',
                    'bg' => 'bg',
                    'bn' => 'bn',
                    'ca' => 'ca',
                    'cs' => 'cs',
                    'cy' => 'cy',
                    'de' => 'de',
                    'el' => 'el',
                    'en' => 'en',
                    'eo' => 'eo',
                    'es' => 'es',
                    'et' => 'et',
                    'eu' => 'eu',
                    'fr' => 'fr',
                    'ga' => 'ga',
                    'hi' => 'hi',
                    'hr' => 'hr',
                    'hu' => 'hu',
                    'id' => 'id',
                    'it' => 'it',
                    'ja' => 'ja',
                    'ko' => 'ko',
                    'nl' => 'nl',
                    'pl' => 'pl',
                    'pt' => 'pt',
                    'ru' => 'ru',
                    'sk' => 'sk',
                    'sl' => 'sl',
                    'sr' => 'sr',
                    'tr' => 'tr',
                    'ur' => 'ur',
                    'zh' => 'zh'
                ),
        );

        # TODO enable filter
        $formDescriptor['select2'] = array(
                'section' => 'Settings',
                'type' => 'select',
                'name' => 'art_type',
                'label' => 'Select the Article Type',
                'options' => array(
                    'All' => 'all',
                    'OntologyClass' => 'class',
                    'OntologyProperty' => 'prop',
                    'Datatype' => 'type'
                ),
        );

        $formDescriptor['info'] = array(
                'section' => 'Settings',
                'type' => 'info',
                'label' => 'Select',
                'default' => '<a href="#" id="toggleall">All</a>, ' .
                             '<a href="#" id="togglenone">None</a>',
                'raw' => true # if true, the above string won't be html-escaped.
        );

        $links = $this->create_limit_links($limit, $offset, $lang);

        $formDescriptor['info2'] = array(
                'section' => 'Settings',
                'type' => 'info',
                'label' => 'Show',
                'default' => join(" | ", $links) .  ' Translations',
                'raw' => true # if true, the above string won't be html-escaped.
        );

        $form = new HTMLForm( $formDescriptor, $this->getContext() );

        $form->setMethod('get');

        $params = array(
            'limit'  => $limit,
            'offset' => $offset,
            'lang' => $lang,
            'art_type' => 'all',
        );
        $form->setAction($this->getTitle()->getLocalURL() . '?' . http_build_query($params));

        $form->setSubmitText( 'Show Translations' );

        $form->show();

    }

    protected function create_limit_links($curr_limit, $offset, $lang) {
        $limits = array( 10, 20, 50, 100, 150 );


        foreach( $limits as $limit ) {
            $params = array(
                'limit'  => $limit,
                'offset' => $offset,
                'lang' => $lang,
                'art_type' => 'all',
            );

            if($limit != $curr_limit) {
                $links[] =  '<a href="' . $this->getTitle()->getLocalURL() . '?' . http_build_query($params) . '" id="limit">' . $limit . '</a>';
            } else {
                $links[] = "<b>" . (string) $limit . "</b>";
            }
        }

        return $links;
    }

    protected function show_table($missing, $lang , $limit, $offset) {
        $output = $this->getOutput();

        $output->addHTML( '<form id="approve">');

        $output->addHTML( "<input type='submit' value='Approve'></input>" );
        $output->addHTML( "<p />" );
        $output->addHTML( "<input type='hidden' name='language' value='". $lang . "' >" );

        $output->addHTML( "<table class='mw-datatable'>");
        $output->addHTML( "<th>Article</th>");
        $output->addHTML( "<th>english Label</th>");
        $output->addHTML( "<th>Translation</th>");
        $output->addHTML( "<th>Action</th>" );

        foreach ( array_slice($missing, $offset, $limit) as $row ) {
            $output->addHTML( "<tr>" );
            $output->addHTML( "<td>" );
            $output->addWikiText("[[" . $row['title'] . "]]" );
            $output->addHTML( "</td>" );
            $output->addHTML( "<td>" );
            $output->addHTML( $row['label'] );
            $output->addHTML( "</td>" );
            $output->addHTML( "<input type='hidden' name='title'  value='". $row['title'] . "' >" );
            $output->addHTML( "<td align='left'><input type='text' name='translation' size='40' value='" );
            $output->addHTML( $row['translation'] );
            $output->addHTML( "'></input>" );
            $output->addHTML( "</td>" );
            $output->addHTML( "<td>" );
            $output->addHTML("<input type='checkbox'>");
            $output->addHTML( "</td>" );
            $output->addHTML( "</tr>" );
        }

        $output->addHTML( "</table>" );
        $output->addHTML( "<p />" );
        $output->addHTML( "<input type='submit' value='Approve' />" );
        $output->addHTML( "</form>" );
    }

    protected function show_pagination($limit, $offset, $lang, $max) {
        $output = $this->getOutput();

        $prev = $offset - $limit;

        $params = array(
            'limit'  => $limit,
            'offset' => $prev,
            'lang' => $lang,
            'art_type' => 'all',
        );

        if($prev >= 0) {
            $output->addHTML('<a href="' . $this->base_url . '?' . http_build_query($params) . '"> &lt; Previous </a>');
        }

        $next = $offset + $limit;

        $params = array(
            'limit'  => $limit,
            'offset' => $next,
            'lang' => $lang,
            'art_type' => 'all',
        );

        if($next < $max) {
            $output->addHTML('<a href="' . $this->base_url . '?' . http_build_query($params) . '" > Next &gt; </a>');

        }
    }

}
