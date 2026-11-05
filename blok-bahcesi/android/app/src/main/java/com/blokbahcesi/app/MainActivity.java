package com.blokbahcesi.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(PlayGamesPlugin.class); // yerel Play Games köprüsü
        super.onCreate(savedInstanceState);
    }
}
