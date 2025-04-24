package grool.server;

public class VOMSServer {

    public VOMSServer() {
    }

    public VOMSServer(String DN, String name, int port, String host) {
    }

    public String getDN() {
        return "DummyVOMSDN";
    }

    public String getName() {
        return "DummyVOMSName";
    }

    public int getPort() {
        return 1;
    }

    public String getHost() {
        return "DummyVOMSHost";
    }

    public void setDN(String DN) { }

    public void setName(String name) {}

    public void setPort(int port) {}

    public void setHost(String host) {}

    public static VOMSServer getDefaultVOMSServer() {
        return new VOMSServer();
    }

}
