# Recognize_Speech_No_Dialog_Sample
20190502 the first time sample

Reference:
https://chengabriel.blogspot.com/2016/04/android-dialog.html

首先先在 AndroidManifest.xml 加入一些權限設定
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />

Layout的部分
使用ProgressBar、TextView、ToggleButton三個物件
ProgressBar：用來顯示聲音波動效果
TextView：用來顯示最後便是結果
ToggleButton：用來開啟語音辨識

一般如果直接實作Speech to text的話
使用google api的方式通常會找到下面的方法：
Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
startActivityForResult(intent, RESULT_SPEECH);

但是用這個方式的話就會出現搜尋的框框
所以我們需要自己設定一些東西
implements RecognitionListener
在此已修改為 new RecognitionListener
使用speech = SpeechRecognizer.createSpeechRecognizer(this);
作為 語音辨識 開啟或是關閉
  speech.startListening(recognizerIntent);
or
  speech.stopListening();
  
                
                   
