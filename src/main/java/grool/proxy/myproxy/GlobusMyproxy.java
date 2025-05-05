package grool.proxy.myproxy;

import grool.server.MyproxyServer;
import grool.server.VOMSServer;

import java.io.File;

public class GlobusMyproxy {

    private File proxyFile;

    public GlobusMyproxy(MyproxyServer myproxyServer, VOMSServer vomsServer, File proxy) {
        this.proxyFile = proxy;
    }

    public final boolean isValid() {
        return true;
    }


    public final File getProxy() {
        return this.proxyFile;
    }
}
