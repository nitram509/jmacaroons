package com.github.nitram509.jmacaroons.gwt;

import com.google.gwt.core.client.EntryPoint;
import org.timepedia.exporter.client.ExporterUtil;

public class MacaroonModuleEntryPoint implements EntryPoint {

  public void onModuleLoad() {
    ExporterUtil.exportAll();
    onJMacaroonLoaded();
  }

  private native void onJMacaroonLoaded() /*-{
    if ($wnd.onJMacaroonLoaded) $wnd.onJMacaroonLoaded();
  }-*/;

}