# netclient

Easy make a request in sync or async mode with performance and thread optimisation.
- your response should have at least 2 key, error and value.
- error should be a boolean wha give state of process (true if an error occur, false else)
- value should be you response data.
- auto loggig in debug mode
you can check example
## Gradle
```
compile 'com.steve:netclient:1.0.0'
```
## Maven
```Maven
<dependency>
  <groupId>com.steve</groupId>
  <artifactId>netclient</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```
## You should first init client in your application instance
```Java
public class App extends Application {

    ---

    @Override
    public void onCreate() {
        super.onCreate();
        ---
        //second param is a boolean which enable/disable loggin
        NetClient.init("testApp",BuildConfig.DEBUG);
    }

    ---

}
```
## async Request example
```Java
NetClient.newRequest(testUrl)
            .setBody(body)
            .enqueue(new NetCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject value) {
                    //On success
                }

                @Override
                public void onError(NetError error) {
                    //On Error
                }
            });
```          
## sync Request example
```Java
try {
    NetBody netBody = NetClient.newRequest(testUrl)
            .setBody(body)
            .call();

    String serverResponse = netBody.string();

} catch (NetError netError) {
    netError.printStackTrace();
}
```
### Note
Params is encrypted in base64 per default. but you can override this behavior by providing and instance of NetParamsEncrypting...
```Java
 NetClient.newRequest(testUrl, new NetParamsEncrypting() {
                @Override
                public byte[] encrypt(String params) {
                    byte[] encrypted = ... // encrypt param with your own algorithm or return params.getBytes() if no needed
                    return encrypted;
                }
            })
```
You can also use a queue pattern like in volley
#### example
```Java
NetClient netClient = NetClient.newRequest(testUrl)
                .setBody(body)
                .build();

        NetClient.newNetQueue().addToQueue(netClient);
        
```
