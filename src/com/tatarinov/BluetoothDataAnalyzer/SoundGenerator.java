package com.tatarinov.BluetoothDataAnalyzer;

import java.util.HashMap;
import java.util.Map;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

public class SoundGenerator {   
	
	private class SoundTone {	    
		private Handler handler = new Handler();
		
	    private final double duration = 0.1d;
	    private final int sampleRate = 8000;
	    private final int numSamples = (int)(duration * sampleRate);
	    private final double samples[] = new double[numSamples];	    
	    private final byte generatedSnd[] = new byte[2 * numSamples];	    	   
	    
	    private double freqOfTone;	    
	    private AudioTrack audio;
	    private int playState;

	    public SoundTone(double frequencyOfTone){
	        freqOfTone = frequencyOfTone;
	    }
	    
	    public void stop(){
	    	playState = 0;
	    	if (audio != null){	    		
	    		audio.pause();
	    	}
	    }

	    public void play(){
	    	if (playState > 0)
	    		return;
	    	if (audio != null){
	    		playState = 1;	    	
	    		audio.play();
	    		return;
	    	}	    	
	    	playState = 2;
	        Thread thread = new Thread(new Runnable() {
	            public void run() {
	                generateTone();

	                handler.post(new Runnable() {
	                    public void run() {
	                        playSound();
	                    }
	                });
	            }
	        });
	        thread.start();
	    }

	    private void generateTone(){
	        for (int i = 0; i < numSamples; ++i) {
	            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
	        }
	        
	        int idx = 0;
	        for (double dVal : samples) {
	            short val = (short) (dVal * 32767);
	            generatedSnd[idx++] = (byte) (val & 0x00ff);
	            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
	        }
	    }

	    private void playSound() {        
	        try {
	            final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
	                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
	                    AudioFormat.ENCODING_PCM_16BIT, numSamples*2,
	                    AudioTrack.MODE_STATIC);	            	            
	            audioTrack.write(generatedSnd, 0, numSamples*2);	                      	       	         
	            audioTrack.setLoopPoints(0, numSamples, -1);
	            audio = audioTrack;	            
	            if (playState > 0){
	            	playState = 1;	            	
	            	audio.play();	
	            }	                                      	                      
	        } catch (Exception ex) {	        	 
	            ex.printStackTrace();
	        }
	    }
	}

    private static SoundGenerator instance;    
    public static SoundGenerator getInstance(){
    	if (instance == null){
    		instance = new SoundGenerator();
    	}
    	return instance;
    }          
    
    private Map<Integer, SoundTone> mTonesPool = new HashMap<Integer, SoundTone>();
    private int mCurrentPlayingTone;
       
    private SoundGenerator(){    	
    	mTonesPool.put(1500, new SoundTone(1500));
    	mTonesPool.put(500, new SoundTone(500));
    }   
    
    public void stop() {
    	SoundTone currentTone = mTonesPool.get(mCurrentPlayingTone);    	
    	if (currentTone != null){
    		currentTone.stop(); 
    	}   
    	mCurrentPlayingTone = 0;
    }
  
    public void playTone(int frequencyOfTone) {    	    
    	if (mCurrentPlayingTone == frequencyOfTone)
    		return;    	    
    	
    	this.stop();    	
    	mCurrentPlayingTone = frequencyOfTone;    
    	
    	SoundTone tone = mTonesPool.get(frequencyOfTone);    	
    	if (tone == null){    		
    		tone = new SoundTone(frequencyOfTone);
    		mTonesPool.put(frequencyOfTone, tone);
    	}
    	tone.play();
    }
}
