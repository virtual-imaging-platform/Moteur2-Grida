package grool.server;

public class MyproxyServer {

    public MyproxyServer() {
    }

    public MyproxyServer(String server, int port) {
    }

    public int getPort() {
        return 1;
    }

    public String getHost() {
        return "DummyMyproxyHost";
    }

    public void setPort(int port) {}

    public void setHost(String host) { }

    public static MyproxyServer getDefaultMyproxyServer() {
        return new MyproxyServer();
    }
}
