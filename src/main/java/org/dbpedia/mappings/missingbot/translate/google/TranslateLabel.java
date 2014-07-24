package org.dbpedia.mappings.missingbot.translate.google;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.LanguagesListResponse;
import com.google.api.services.translate.model.LanguagesResource;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Created by peterr on 05.06.14.
 */
public class TranslateLabel {

    /** Global instance of the HTTP transport. */
    private HttpTransport httpTransport;

    private Translate client;
    private static final String src_lang = "en";

    public TranslateLabel(String api_key, String app_name) throws IOException {
        try {
             httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

        // set up global Translate instance
        client = new Translate.Builder(httpTransport, JSON_FACTORY, null)
                .setGoogleClientRequestInitializer(new TranslateRequestInitializer(api_key))
                .setApplicationName(app_name).build();
    }

    public List<String> getLanguages() throws IOException {
        Translate.Languages.List request = client.languages().list();

        LanguagesListResponse langs = request.execute();
        List<LanguagesResource> c = langs.getLanguages();

        List<String> result = new ArrayList<String>();

        for (LanguagesResource d : c){
            result.add(d.getLanguage());
        }

        return result;

    }

    public Map<String, String> translate(List<String> labels, String lang) throws IOException {
        Translate.Translations.List request = client.translations().list(labels, lang);

        // set source language
        request.setSource(src_lang);

        TranslationsListResponse resp = request.execute();
        List<TranslationsResource> res = resp.getTranslations();

        Map<String, String> result = new HashMap<String, String>();

        for (int i = 0; i < labels.size(); i++) {
            result.put(labels.get(i), res.get(i).getTranslatedText());

        }

        return result;

    }

    public String translate(String label, String lang) throws IOException {

        List<String> labels = new LinkedList<String>();
        labels.add(label);

        Map<String, String> res = this.translate(labels, lang);

        return res.get(label);
    }
}
