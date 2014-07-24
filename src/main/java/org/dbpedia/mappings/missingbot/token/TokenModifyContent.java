package org.dbpedia.mappings.missingbot.token;

import net.sourceforge.jwbf.core.actions.Post;
import net.sourceforge.jwbf.core.actions.RequestBuilder;
import net.sourceforge.jwbf.core.actions.util.HttpAction;
import net.sourceforge.jwbf.mediawiki.ApiRequestBuilder;
import net.sourceforge.jwbf.mediawiki.MediaWiki;
import net.sourceforge.jwbf.mediawiki.actions.util.MWAction;
import org.dbpedia.mappings.missingbot.label.TranslateLabelArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TokenModifyContent extends MWAction {

    private static final Logger log = LoggerFactory.getLogger(TokenModifyContent.class);

    static final Charset CHARSET = StandardCharsets.UTF_8;

    private String token;
    private TranslateLabelArticle article;
    static final String PARAM_MINOR = "minor";
    static final String PARAM_MINOR_NOT = "notminor";

    public TokenModifyContent(TranslateLabelArticle article, String token) {
        article.setText(article.build_new_text());
        this.article = article;
        this.token = token;
    }

    @Override
    public HttpAction getNextMessage() {
        Post msg = null;
        if (log.isTraceEnabled()) {
            log.trace("enter TokenModifyContent.getNextMessage");
        }
        RequestBuilder builder = new ApiRequestBuilder() //
                .action("edit") //
                .formatXml() //
                .param("token", MediaWiki.urlEncode(token)) //
                .param("title", MediaWiki.urlEncode(article.getTitle())) //
                .postParam("summary", article.getEditSummary()) //
                .postParam("text", article.getText()) //
                ;

        if (article.isMinorEdit()) {
            builder.postParam(PARAM_MINOR, "");
        } else {
            builder.postParam(PARAM_MINOR_NOT, "");
        }

        msg = builder.buildPost();
        log.debug("url: \"{}\"", msg.getRequest());

        return msg;
    }
}
