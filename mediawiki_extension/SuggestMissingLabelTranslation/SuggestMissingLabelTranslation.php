<?php
# Alert the user that this is not a valid access point to MediaWiki if they try to access the special pages file directly.
if ( !defined( 'MEDIAWIKI' ) ) {
    echo <<<EOT
To install my extension, put the following line in LocalSettings.php:
require_once( "\$IP/extensions/SuggestMissingLabelTranslation/SuggestMissingLabelTranslation.php" );
EOT;
    exit( 1 );
}
# TODO: set rest url as extension config variable to overwrite in LocalSettings

define( 'SMLT_REST_URL',  "http://localhost:9998/missings/");

$wgExtensionCredits[ 'specialpage' ][] = array(
    'path' => __FILE__,
    'name' => 'SuggestMissingLabelTranslation',
    'author' => 'coderiot@github',
    'url' => '',
    'descriptionmsg' => 'myextension-desc',
    'version' => '0.1',
);

$wgGroupPermissions['sysop']['smlt'] = true;
$wgAvailableRights[] = 'smlt';

$dir = dirname(__FILE__) . '/';
$wgAutoloadClasses[ 'SpecialSuggestMissingLabelTranslation' ] = $dir . '/SpecialSuggestMissingLabelTranslation.php'; # Location of the SpecialMyExtension class (Tell MediaWiki to load this file)
$wgExtensionMessagesFiles[ 'SuggestMissingLabelTranslation' ] = $dir . '/SuggestMissingLabelTranslation.i18n.php'; # Location of a messages file (Tell MediaWiki to load this file)
$wgExtensionMessagesFiles[ 'SuggestMissingLabelTranslationAlias' ] = $dir . '/SuggestMissingLabelTranslation.alias.php'; # Location of an aliases file (Tell MediaWiki to load this file)
$wgSpecialPages[ 'SuggestMissingLabelTranslation' ] = 'SpecialSuggestMissingLabelTranslation'; # Tell MediaWiki about the new special page and its class name
$wgSpecialPageGroups[ 'SuggestMissingLabelTranslation' ] = 'pagetools';

$wgResourceModules['ext.ssml'] = array(
    'scripts' => 'ext.ssml.js',
    'dependencies' => array(
            'jquery.cookie',
                ),
                'localBasePath' => $dir,
                'remoteExtPath' => 'SuggestMissingLabelTranslation',
);
