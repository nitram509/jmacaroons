package com.googlecode.cryptogwt.provider;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

public class EventEntropySource extends EntropySource {

    public static int ENTROPY_ID = 0x101;

    public static double ENTROPY_ESTMATE = 3.3;

    private HandlerRegistration handlerRegistration;

    private Event.NativePreviewHandler collectEvents = new Event.NativePreviewHandler() {

        int previousScreenX = 0;
        int previousScreenY = 0;
        long previousSystemTime = 0;

        public void onPreviewNativeEvent(NativePreviewEvent event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            int button = nativeEvent.getButton();
            int x = nativeEvent.getClientX();
            int y = nativeEvent.getClientY();            
            int keyCode = nativeEvent.getKeyCode();
            int mouseWheelVelocity = nativeEvent.getMouseWheelVelocityY();            
            int screenX = nativeEvent.getScreenX();
            int screenY = nativeEvent.getScreenY();
            int keyModifiers =
                ((nativeEvent.getAltKey() ? 8 : 0) |
                (nativeEvent.getCtrlKey() ? 4 : 0) | 
                (nativeEvent.getMetaKey() ? 2 : 0) |
                (nativeEvent.getShiftKey() ? 1 : 0));
            byte[] compressedData;
            long currentTime = System.currentTimeMillis();
            byte timeDiff = (byte) ((currentTime - previousSystemTime) & 0xff);
            byte eventType = (byte) (event.getTypeInt() & 0xff);
            if (keyCode != 0) {
                compressedData = new byte[] {
                        timeDiff, eventType,
                        (byte) (keyCode & 0xff),
                        (byte) keyModifiers };                
            } else {
                compressedData = new byte[] {
                        timeDiff,
                        eventType,
                        (byte) ((screenX - previousScreenX) & 0xff),
                        (byte) ((screenY - previousScreenY) & 0xff),
                        (byte) (button << 4 | keyModifiers),
                        (byte) mouseWheelVelocity };
            }
            previousScreenX = screenX;
            previousScreenY = screenY;
            previousSystemTime = currentTime;
            addEntropy(ENTROPY_ID, ENTROPY_ESTMATE, compressedData);
        }
    };

    public EventEntropySource() {        
    }

    @Override
    public void startCollecting() {
        if (handlerRegistration != null) return;
        handlerRegistration = Event.addNativePreviewHandler(collectEvents);
    }

    @Override
    public void stopCollecting() {
        if (handlerRegistration == null) return;
        handlerRegistration.removeHandler();
        handlerRegistration = null;
    }

    @Override
    public boolean isCollecting() {
        return handlerRegistration != null;
    }

}
