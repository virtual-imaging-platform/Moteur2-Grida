package fr.insalyon.creatis.moteur2;

import java.io.*;
import java.util.Properties;

/**
 * Created by abonnet on 1/30/19.
 *
 * TODO : at the moment, logs are done with System.[out|err].println
 * That's OK because when moteur2 is started, standard output and error are
 * forwarded to log files, but it would better to do that through a logging system
 */
public enum Configuration {
    INSTANCE;

    private final String CONF_FILE = "moteur2-grida.conf";
    private final String MOTEUR_DIR = "/.moteur2/";

    private final String GRIDA_HOST = "grida.server.host";
    private final String GRIDA_PORT = "grida.server.port";
    private final String PROXY_PATH = "proxy.path";

    private final String DOWNLOAD_RETRY_IN_SEC = "download.retry-in-seconds";
    private final String DOWNLOAD_TIMEOUT_IN_SEC = "download.timeout-in-seconds";
    private final Integer DEFAULT_DOWNLOAD_RETRY = 5; // in seconds
    private final Integer DEFAULT_DOWNLOAD_TIMEOUT = 900; // in seconds

    private String gridaHost;
    private Integer gridaPort;
    private String serverProxy;
    private int downloadRetryInSec;
    private long downloadTimeoutInSec;


    Configuration() {

        String configurationFolder = System.getenv("HOME") + MOTEUR_DIR;
        String confFilePath = configurationFolder + CONF_FILE;
        assertFileExistence(confFilePath);

        // load Configuration File
        Properties props = loadPropertiesFile(confFilePath);

        // check grida properties
        gridaHost = getAndCheckProperty(props, GRIDA_HOST);
        gridaPort = getAndCheckIntProperty(props, GRIDA_PORT);
        serverProxy = getAndCheckProperty(props, PROXY_PATH);
        assertFileExistence(serverProxy);

        // load others parameters
        downloadRetryInSec = getIntProperty(props, DOWNLOAD_RETRY_IN_SEC, DEFAULT_DOWNLOAD_RETRY);
        downloadTimeoutInSec = getIntProperty(props, DOWNLOAD_TIMEOUT_IN_SEC, DEFAULT_DOWNLOAD_TIMEOUT);
    }

    private void assertFileExistence(String path) {
        if ( ! (new File(path).exists())) {
            System.err.println("mandatory file does not exist : " + path);
            throw new IllegalStateException("Missing file : " + path);
        }
    }

    private Properties loadPropertiesFile(String path) {
        Properties props = new Properties();
        File file = new File(path);
        try(InputStream is = new FileInputStream(file)) {
            props.load(is);
            return props;
        } catch (IOException e) {
            System.err.println("Error loading properties File : " + path);
            throw new IllegalStateException("Error loading properties File : " + path);
        }
    }

    private String getAndCheckProperty(Properties props, String key){
        String value = props.getProperty(key);
        if (value == null) {
            System.err.println("Missing property : " + key);
            throw new IllegalStateException("Missing property : " + key);
        }
        return value;
    }

    private Integer getAndCheckIntProperty(Properties props, String key){
        String stringValue = getAndCheckProperty(props, key);
        return Integer.valueOf(stringValue);
    }

    private Integer getIntProperty(Properties props, String key, Integer defaultValue){
        String stringValue = props.getProperty(key, defaultValue.toString());
        return Integer.valueOf(stringValue);
    }

    private Long getLongProperty(Properties props, String key, Long defaultValue){
        String stringValue = props.getProperty(key, defaultValue.toString());
        return Long.valueOf(stringValue);
    }

    /* Properties getters */

    public String getGridaHost() {
        return gridaHost;
    }

    public Integer getGridaPort() {
        return gridaPort;
    }

    public String getServerProxy() {
        return serverProxy;
    }

    public int getDownloadRetryInSec() {
        return downloadRetryInSec;
    }

    public long getDownloadTimeoutInSec() {
        return downloadTimeoutInSec;
    }
}
