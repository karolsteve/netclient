# netclient

easy make a request in sync or async mode.
your response should have at least 2 key, error and value.
error should be a boolean wha give state of process (true if an error occur, false else)
value should be you respnse data.
you can check example

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
###Note
Params is encrypted in base64 per default. but you can override this behavior by providing and instance of NetParamsEncrypting...
```Java
 NetClient.newRequest(testUrl, new NetParamsEncrypting() {
                @Override
                public byte[] encrypt(String params) {
                    return new byte[0];
                }
            })
```
