package com.googlecode.cryptogwt.provider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;

/**
 * Adds an incremental command to the Javascript runloop and updates with time differences
 * between subsequent invocations.  Expensive/low-quality source that is just used until
 * sufficient seed is available.
 *
 */
public class RunLoopEntropySource extends EntropySource {
    
    private static final double ENTROPY_ESTIMATE = 0.5;

    int ENTROPY_ID = 0x100;    
    
    private long last = System.currentTimeMillis();
    
    private boolean isCollecting = false;
    
    private int frequencies[] = new int[256];
    
    private double runningAverage = 0;
    
    private int n;
    
    private IncrementalCommand cmd = new IncrementalCommand() {        
        public boolean execute() {
           if (!isCollecting) return false;
           long current = System.currentTimeMillis();           
           final int difference = (int) (current - last);
           runningAverage = (difference + runningAverage) / ++n;
           if (n < 10) return true; // TODO: Calculate a proper minimum n
           byte sample = (byte) (Math.round(runningAverage - difference) & 0xff); 
           frequencies[sample + 128]++;
           addEntropy(ENTROPY_ID, ENTROPY_ESTIMATE, new byte[] { sample } );
           last = current;
           if (!getSink().needsEntropy()) {
               isCollecting = false;
           }
           return getSink().needsEntropy();
        }
    };
    
    public RunLoopEntropySource(EntropySink sink) {
        super(sink);
    }
    
    public RunLoopEntropySource() { }
    
    @Override
    public void startCollecting() {
        if (!isCollecting) {
            isCollecting = true;
            DeferredCommand.addCommand(cmd);
        }        
    }

    @Override
    public void stopCollecting() {
        isCollecting = false;
        for (int i = 0; i < frequencies.length; i++) {
            GWT.log("[" + i + "] " + frequencies[i], null);
        }
    }

    @Override
    public boolean isCollecting() {
       return isCollecting;
    }
}
