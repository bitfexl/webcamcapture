package com.github.bitfexl.webcamcapture;

import com.github.bitfexl.webcamcapture.config.ApplicationConfig;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main {
    public static void main(String[] args) {
        if (args.length == 1) {
            ApplicationConfig.setConfigFile(args[0]);
        } else if (args.length > 1) {
            Log.error("Invalid number of arguments. Expected a single argument: the config file path, defaults to 'webcamcaptureconfig.json' if omitted.");
        } else {
            ApplicationConfig.setConfigFile("webcamcaptureconfig.json");
        }

        Quarkus.run(args);
    }
}
