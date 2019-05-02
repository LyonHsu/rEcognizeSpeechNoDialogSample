package speech.lyon.google.recognize_speech_no_dialog_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import speech.lyon.google.recognize_speech_no_dialog_sample.Permissions.PermissionsActivity;
import speech.lyon.google.recognize_speech_no_dialog_sample.Permissions.PermissionsChecker;

public class MainActivity extends AppCompatActivity {
    static String TAG = "Speech Lyon";
    boolean isDefault = false;
    private final int RESULT_SPEECH = 100;
    private TextView returnedText;
    private ProgressBar progressBar;
    private ToggleButton toggleButton;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    RecognitionListener recognitionListener;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    public static final int REQUEST_CODE = 2; // 请求码
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(TAG, "destroy");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d(TAG, "REQ_CODE_SPEECH_INPUT result" + result);
                }else{
                    Log.e(TAG,"REQ_CODE_SPEECH_INPUT fail 辨識失敗");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if user has given permission to record audio
        mPermissionsChecker = new PermissionsChecker(this);
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
        //default
        if(isDefault){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            startActivityForResult(intent, RESULT_SPEECH);
        }else{
            init();
            recognitionListener =new RecognitionListener(){

                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.i(TAG, "onReadyForSpeech");
                    Log.e(TAG, "准备就绪，可以开始说话");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.i(TAG, "onBeginningOfSpeech");
                    progressBar.setIndeterminate(false);
                    progressBar.setMax(10);
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    Log.i(TAG, "onRmsChanged: " + rmsdB);
                    progressBar.setProgress((int) rmsdB);
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.i(TAG, "onBufferReceived: " + buffer);
                }

                @Override
                public void onEndOfSpeech() {
                    Log.i(TAG, "onEndOfSpeech");
                    progressBar.setIndeterminate(true);
                    toggleButton.setChecked(false);
                }

                @Override
                public void onError(int errorCode) {
                    String errorMessage = getErrorText(errorCode);
                    Log.d(TAG, "FAILED " + errorMessage);
                    returnedText.setText(errorMessage);
                    toggleButton.setChecked(false);
                }

                @Override
                public void onResults(Bundle results) {
                    Log.i(TAG, "onResults");
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String text = "";
                    for (String result : matches){
                        text += result + "\n";
                    }
                    returnedText.setText(text);
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    Log.i(TAG, "onPartialResults");
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.i(TAG, "onEvent");
                }
            };
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(recognitionListener);
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        }
    }

    private void init(){
        returnedText = (TextView)findViewById(R.id.textView1);
        progressBar = (ProgressBar)findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton)findViewById(R.id.toggleButton1);
        progressBar.setVisibility(View.INVISIBLE);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                }else{
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });
    }

    private static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error 音频问题";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error 其它客户端错误";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions 权限不足";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error 网络问题";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout 网络连接超时";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match 没有匹配的识别结果";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy 引擎忙";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server 服务端错误";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input 没有语音输入";
                break;
            default:
                message = "Didn't understand, please try again. 识别失败";
                break;
        }
        Log.e(TAG, ""+message);
        return message;
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }


}
