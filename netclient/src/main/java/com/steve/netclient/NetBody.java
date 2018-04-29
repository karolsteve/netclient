package com.steve.netclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Classe responsable du corps de la requête que retourne le serveur, après un appel à call() de {@link NetClient}
 * @author Steve Tchatchouang
 */
public class NetBody {
    private static final String TAG = NetBody.class.getSimpleName();

    private InputStream inputStream;
    private int responseCode;
    private String responseMessage;
    private String responseText;

    /**
     * Retourne le flux d'entré de la réponse
     * @return flux d'entré
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null){
                sb.append(line);
            }
            this.responseText = sb.toString();
            NetLog.m("Response Text : "+this.responseText);
        } catch (IOException e) {
            NetLog.e(e);
        }
    }

    void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Retourne le code de la réponse
     * <p>Exemple : 404, 200 etc...</p>
     * @return code de la réponse
     */
    public int getResponseCode() {
        return responseCode;
    }

    void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Retourne le Message de la réponse
     * <p>Exemple : OK, etc...</p>
     * @return chaine de caractère représentant le message de la réponse
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Obtenir le résultat de execute() d'{@link NetClient} sous forme de chaine de caractère
     * @return le corps de la requête retourné par le serveur ou null en cas d'erreur
     */
    public String string(){
        return responseText;
    }

    /**
     * @return le corps de la requête retourné par le serveur sous forme de text, ou null en cas d'erreur
     */
    @Override
    public String toString() {
        return responseText;
    }
}
