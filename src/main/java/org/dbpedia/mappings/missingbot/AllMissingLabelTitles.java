package org.dbpedia.mappings.missingbot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllMissingLabelTitles implements Iterable<String>  {

    private final String url;
    private final String filter;
    public int length = 0;

    public AllMissingLabelTitles(String language, String filter) {
        this.url = String.format("http://mappings.dbpedia.org/server/ontology/labels/missing/%s/", language);
        this.filter = filter;
    }

    private ArrayList<String> getMissingLinks() {
        ArrayList<String> links = new ArrayList<String>();

        String line;
        StringBuilder builder = new StringBuilder();

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(this.url);

        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();

                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(instream));
                    while ((line = rd.readLine()) != null) {
                        builder.append(line);
                        builder.append("\n");
                    }
                } finally {
                    instream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String pattern = "href=\"(.*?)\"";
        Matcher m = Pattern.compile(pattern).matcher(builder.toString());

        while(m.find()) {
            links.add(m.group(1));
        }

        return links;
    }

    public Iterator<String> iterator() {
        ArrayList<String> missingLinks = getMissingLinks();

        this.length = 0;

        Collection<String> missingTitles = new ArrayList<String>();

        for (String link : missingLinks) {
            String[] splits = link.split("/");
            String title = splits[splits.length -1];

            if(!title.startsWith(this.filter)) {
                continue;
            }

            missingTitles.add(title);
            this.length++;
        }

        return missingTitles.iterator();
    }
}
