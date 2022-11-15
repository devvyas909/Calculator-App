package com.example.mcalcpro;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ca.roumani.i2c.MPro;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,SensorEventListener {
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tts=new TextToSpeech (this,this);
        SensorManager sm=(SensorManager) getSystemService(SENSOR_SERVICE);
        sm.registerListener(this,sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onInit(int initStatus){
        this.tts.setLanguage(Locale.US);
    }
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1){}
    @Override
    public void onSensorChanged(SensorEvent event){
        double ax=event.values[0];
        double ay=event.values[1];
        double az=event.values[2];
        double a=Math.sqrt(ax*ax+ay*ay+az*az);
        if(a>20){
            ((EditText) findViewById(R.id.pBox)).setText("");
            ((EditText) findViewById(R.id.aBox)).setText("");
            ((EditText) findViewById(R.id.iBox)).setText("");
            ((TextView) findViewById(R.id.output)).setText("");

        }
    }

    public void computePayment(View v){
        EditText p= findViewById(R.id.pBox);
        EditText a= findViewById(R.id.aBox);
        EditText i= findViewById(R.id.iBox);

        String sp=p.getText().toString();
        String sa=a.getText().toString();
        String si=i.getText().toString();

        MPro mPro=new MPro();

        try{
            mPro.setPrinciple(sp);
            mPro.setAmortization(sa);
            mPro.setInterest(si);
        }catch(Exception e){
            Toast message= Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
            message.show();
        }
        String computePayment=mPro.computePayment("%,.2f");
        tts.speak(computePayment,TextToSpeech.QUEUE_FLUSH,null);

        String payment=String.format("Monthly Payment= %s",mPro.computePayment("%,.2f"));

        StringBuilder outstandingAfterText= new StringBuilder(String.format("%s %n%nBy making this payments monthly for %s years, the mortgage will be paid in full. But if you terminate the mortgage on its nth anniversary, the balance still owing depends on n as shown below: %n%n" + "   n             Balance    %n%n", payment, mPro.getAmortization()));
        int j=0;
        while(j<21){
            outstandingAfterText.append(String.format(Locale.US, "   %d    %s %n%n", j, mPro.outstandingAfter(j, "%,16.0f")));
            if(j<5){
                j++;
            }else{
                j+=5;
            }
        }
        ((TextView) findViewById(R.id.output)).setText(payment);

        ((TextView) findViewById(R.id.output)).setText(outstandingAfterText.toString());



    }




}