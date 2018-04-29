package com.steve.netclient;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.steve.netclient.encrypting.NetBase64ParamsEncrypting;
import com.steve.netclient.encrypting.NetParamsEncrypting;

/**
 * Cette class représente le client pour les differents web services de Adwa
 * <p>Pour obtenir une instance de celle ci, on passe par le Builder</p>
 * créé le 15/12/2017
 *
 * @author Steve Tchatchouang
 * @see Builder
 * @see NetBody
 * @see NetCallback
 * @see NetError
 */
public class NetClient implements Callable<NetBody> {

    private static final int CPU_COUNT      = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(CPU_COUNT - 1, 2);
    private static final int MAX_POOL_SIZE  = Math.max(CPU_COUNT, 5);
    private static final int KEEP_ALIVE_IME = 2;
    private static final int QUEUE_CAPACITY = 100;

    private static final String ERROR = "error";
    private static final String VALUE = "value";

    private final static ExecutorService sExecutorService;

    static String  applicationName;
    static boolean debugMode;

    static {
        sExecutorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_IME,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>(QUEUE_CAPACITY)
        );
    }

    private String  url;
    private Method  method;
    private byte[]  body;
    private int     readTimeOut;
    private int     connectTimeOut;
    private String  contentType;
    private Handler handler;

    private NetClient(Builder builder) {
        if (applicationName == null || applicationName.trim().length() == 0)
            throw new RuntimeException("Please call init with valid app Name");
        this.url = builder.url;
        this.method = builder.method;
        this.body = builder.body;
        this.readTimeOut = builder.readTimeOut;
        this.connectTimeOut = builder.connectTimeOut;
        this.contentType = builder.contentType;
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Execute la requête dans un nouveau Thread puis passe le résultat dans le Callback {@link NetCallback}
     * <p>NB: le callback est appelé dans le Thread Principale</p>
     *
     * @see NetCallback ,Thread
     */
    <T> void enqueue(final NetCallback<T> netCallback) {
        sExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NetBody netBody = call();
                    final JSONObject jsonObject = new JSONObject(netBody.string());
                    if (jsonObject.getBoolean(ERROR)) {
                        notifyError(new NetError(NetError.ErrorType.RETURN_FALSE_ERROR), netCallback);
                    } else {
                        notifySuccess(jsonObject.get(VALUE), netCallback);
                    }
                } catch (final NetError error) {
                    notifyError(error, netCallback);
                    error.printStackTrace();
                    NetLog.e(error);
                } catch (JSONException e) {
                    notifyError(new NetError(NetError.ErrorType.SERVER_ERROR), netCallback);
                    NetLog.e("Json Error : ", e);
                }
            }
        });
    }

    private <T> void notifySuccess(final Object object, final NetCallback<T> netCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                netCallback.onSuccess((T) object);
            }
        });
    }

    private void notifyError(final NetError error, final NetCallback netCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                netCallback.onError(error);
            }
        });
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.equals("");

        } catch (Exception e) {
            NetLog.e(e);
            return false;
        }
    }

    private NetBody getInputStream() throws IOException, NetError {
        if (!isInternetAvailable()) {
            NetLog.m("Network not available");
            throw new NetError(NetError.ErrorType.CONNECTION_ERROR);
        }
        URL urlAddress = new URL(this.url);
        NetLog.m("New connection to " + url);
        long startTime = System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) urlAddress.openConnection();
        connection.setRequestMethod(this.method == Method.GET ? "GET" : "POST");
        NetLog.m("Request method " + connection.getRequestMethod());
        connection.setDoInput(true);
        connection.setReadTimeout(this.readTimeOut);
        connection.setConnectTimeout(this.connectTimeOut);
        connection.setRequestProperty("Content-Type", contentType);
        NetLog.m("Content type " + contentType);
        if (body != null) {
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(body);
            os.close();
            NetLog.m("params " + (new String(Base64.decode(new String(body), Base64.NO_WRAP))));
            NetLog.m("Encoded params " + new String(body));
        }
        connection.connect();

        NetLog.m("Response code : " + connection.getResponseCode());
        NetLog.m("Response message : " + connection.getResponseMessage());
        long endTime = System.currentTimeMillis();
        NetLog.m("Total Time : " + (endTime - startTime) + " millis");
        NetBody netBody = new NetBody();
        netBody.setInputStream(connection.getInputStream());
        netBody.setResponseCode(connection.getResponseCode());
        netBody.setResponseMessage(connection.getResponseMessage());
        connection.disconnect();
        NetLog.m("Connection done : final time " + (System.currentTimeMillis() - startTime) + " millis");
        return netBody;
    }

    /**
     * Execute la requête dans le Thread courant en bloquant par conséquent celui ci, puis retourne le résultat
     *
     * @return NetBody, objet permettant d'obtenir les informations sur le résultat
     * @throws NetError exception en cas d'erreur lors de l'execution
     * @see NetBody , NetError
     */
    @Override
    public NetBody call() throws NetError {
        try {
            return getInputStream();
        } catch (IOException e) {
            NetLog.e(e);
            throw new NetError(e);
        }
    }

    public static Builder newRequest(String url) {
        return new Builder(url);
    }

    public static Builder newRequest(String url,NetParamsEncrypting encrypting) {
        return new Builder(url, encrypting);
    }

    public static void init(String appName, boolean debug) {
        applicationName = appName;
        debugMode = debug;
    }

    /**
     * Classe permettant de construire un objet {@link NetClient}
     */
    public static class Builder {

        private static final int    DEFAULT_TIMEOUT      = 50000;
        private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

        private String url;
        private Method method;
        private byte[] body;
        private int    readTimeOut;
        private int    connectTimeOut;
        private String contentType;

        private NetParamsEncrypting encrypting;

        /**
         * Permet d'instancier un {@link Builder}
         *
         * @param url url à atteindre
         */
        public Builder(String url) {
            this.url = url;
            this.readTimeOut = DEFAULT_TIMEOUT;
            this.connectTimeOut = DEFAULT_TIMEOUT;
            this.method = Method.POST;
            this.contentType = DEFAULT_CONTENT_TYPE;
            encrypting = new NetBase64ParamsEncrypting();
        }

        public Builder(String url, NetParamsEncrypting encrypting) {
            this(url);
            this.encrypting = encrypting;
        }

        /**
         * Définir le temps maximal de connexion
         *
         * @param timeOut temps maximal de connexion, en millisecondes
         *                <p>La valeur par défaut est 5000, soit 5 secondes</p>
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         */
        public Builder setConnectionTimeOut(int timeOut) {
            this.connectTimeOut = timeOut;
            return this;
        }

        /**
         * Définir le temps maximal de lecture des données
         *
         * @param timeOut temps maximal de lecture, en millisecondes
         *                <p>La valeur par défaut est 5000, soit 5 secondes</p>
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         */
        public Builder setReadTimeOut(int timeOut) {
            this.readTimeOut = timeOut;
            return this;
        }

        /**
         * Définir le type de contenu du corps de la requête
         *
         * @param contentType type de contenu sous forme de chaine
         *                    <p>le type par défaut est "application/x-www-form-urlencoded"</p>
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         */
        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Définir la méthode de connexion
         *
         * @param method enumeration représentant la méthode : POST par défaut
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         * @see Method
         */
        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        /**
         * Définir le corps de la requête
         * <p>Le corps ne sera plus chiffré</p>
         *
         * @param encodedBody corps de la requête chiffré
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         */
        public Builder setEncodedBody(byte[] encodedBody) {
            this.body = encodedBody;
            return this;
        }

        /**
         * Définir le corps de la requête
         * <p>Le corps sera automatiquement chiffré avec l'algorithme de chiffrement d'Adwa</p>
         *
         * @param body corps de la requête non encodé
         * @return l'instance actuelle du Builder, pour d'éventuelles modifications
         */
        public Builder setBody(String body) {
            return setEncodedBody(encrypting.encrypt(body));
        }

        /**
         * Construit l'instance de {@link NetClient}
         *
         * @return une instance de {@link NetClient}
         */
        public NetClient build() {
            return new NetClient(this);
        }

        /**
         * Construit et Execute la requête dans un nouveau Thread puis passe le résultat dans le Callback {@link NetCallback}
         * <p>NB: le callback est appelé dans le Thread Principale</p>
         *
         * @see NetCallback ,Thread
         */
        public <T> void enqueue(NetCallback<T> netCallback) {
            build().enqueue(netCallback);
        }

        /**
         * Construit et Execute la requête dans le Thread courant en bloquant par conséquent celui ci, puis retourne le résultat
         *
         * @return NetBody, objet permettant d'obtenir les informations sur le résultat
         * @throws NetError exception en cas d'erreur lors de l'execution
         * @see NetBody , NetError
         */
        public NetBody call() throws NetError {
            return build().call();
        }
    }

    /**
     * Enumération des méthodes d'Adwa
     */
    public enum Method {
        POST, GET
    }

    /**
     * Permet d'obtenir une file pour l'execution synchrone des requêtes
     *
     * @return une instance d'{@link NetQueue}
     */
    public static NetQueue newAdwaNetQueue() {
        return new NetQueue();
    }
}
