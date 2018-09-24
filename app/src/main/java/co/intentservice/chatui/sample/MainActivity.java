package co.intentservice.chatui.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

//---------------------
import co.intentservice.*;
//---------------------
// floating widget
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIListener;
import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
// api.ai import
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
// fb api import
//import com.facebook.FacebookSdk;
//import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getName();
    private Gson gson = GsonFactory.getGson();
    public String usermsg;
    final Context context = this;
    private static final int APP_PERMISSION_REQUEST = 102;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //floating widget ------------------------------
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        } else {
           initializeView();
            //startService(new Intent(MainActivity.this, FloatWidgetService.class));
           // alert_msg("widget working");
        }
        */
        //----------------------------------------------


        ChatView chatView = (ChatView) findViewById(R.id.chat_view);
        //chatView.addMessage(new ChatMessage("Message received", System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
        //chatView.addMessage(new ChatMessage("Message sent", System.currentTimeMillis(), ChatMessage.Type.SENT));

        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener() {
            @Override
            public boolean sendMessage(ChatMessage chatMessage) {
                usermsg=chatMessage.getMessage();
                        //alert_msg("START bLITKERZING");
                    send_req(usermsg);

                return true;
            }
        });

        chatView.setTypingListener(new ChatView.TypingListener() {
            @Override
            public void userStartedTyping() {

            }

            @Override
            public void userStoppedTyping() {

            }
        });
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            initializeView();
            //startService(new Intent(this, FloatWidgetService.class));
            //alert_msg("widget working");
        } else {
            Toast.makeText(this, "Draw over other app permission not enable.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeView() {

        //Button mButton= (Button) findViewById(R.id.button);

       // mButton.setOnClickListener(new View.OnClickListener() {
         //   @Override
          //  public void onClick(View view) {
                startService(new Intent(this, co.intentservice.chatui.fab.FloatingActionButton.class));
                alert_msg("---- Widget working ---");
            //     finish();
           // }
        //});
    }


*/


    public void send_msg_to_ui(String msg)
      {
          ChatView chatView = (ChatView) findViewById(R.id.chat_view);
          chatView.addMessage(new ChatMessage(msg, System.currentTimeMillis(), ChatMessage.Type.RECEIVED));
      }
    public  void alert_msg(String s)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder
                .setMessage(" ************ " + s + " **********"+"\n\n ")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        //initializeView();

                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        MainActivity.this.finish();
                    }
                });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    private void send_req(String Query) {

       final AIConfiguration config = new AIConfiguration("841e12cc4b8648989942c403be9b6c4c",
               AIConfiguration.SupportedLanguages.English,
               AIConfiguration.RecognitionEngine.System);

       final AIDataService aiDataService = new AIDataService(this,config);

       //final AIRequest aiRequest = new AIRequest();
       //aiRequest.setQuery(Query);


       final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

           private AIError aiError;

           @Override
           protected AIResponse doInBackground(final String... params) {
               final AIRequest request = new AIRequest();
               String query = params[0];
               String event = params[1];

               if (!TextUtils.isEmpty(query))
                   request.setQuery(query);
               if (!TextUtils.isEmpty(event))
                   request.setEvent(new AIEvent(event));
               final String contextString = params[2];
               RequestExtras requestExtras = null;
               if (!TextUtils.isEmpty(contextString)) {
                   final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                   requestExtras = new RequestExtras(contexts, null);
               }

               try {
                   return aiDataService.request(request, requestExtras);
               } catch (final AIServiceException e) {
                   aiError = new AIError(e);
                   return null;
               }
           }
           @Override
           protected void onPostExecute(final AIResponse response) {
               if (response != null) {
                   onResult(response);
               } else {
                   Log.i(TAG,aiError.toString());
                   send_msg_to_ui(aiError.toString());
                   //onError(aiError);
               }
           }

       };
        task.execute(Query,null,"");
   }



    private void onResult(final AIResponse response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");
                    //Final result
               // resultTextView.setText(gson.toJson(response));

              //  String str=gson.toJson(response);

                 //send_msg_to_ui(gson.toJson(response));

                final Result result = response.getResult();
                final String speech = result.getFulfillment().getSpeech();

                send_msg_to_ui(speech);

                Log.i(TAG, "Speech: " + speech);
                Log.d(TAG,gson.toJson(response) );

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

              //  final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }

        });
    }

}
