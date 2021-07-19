package io.github.trojan_gfw.igniter.bean;

/***************
 ***** Created by fan on 2020/6/24.
 ***************/
public class ServerIp {
    private String ip;
    private String port;
    public ServerIp(String ip,String port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
