package com.olympus.olympus.framework.config;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;


public class Common {
    private static Common instance;
    private Properties servletProp;
    private SERVER_TYPE serverType;
    private DB_TYPE dbType;
    private String currentIP;
    public static final String USER_INFO_PROP = "USER_INFO";
    private HashMap<String, String> services;

    public String getCurrentIP() {
        return this.currentIP;
    }

    public void setCurrentIP(String currentIP) {
        this.currentIP = currentIP;
    }

    public Common() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("/conf/application.properties");
        InputStream isDev = this.getClass().getClassLoader().getResourceAsStream("/conf/application_dev.properties");
        InputStream isLocal = this.getClass().getClassLoader().getResourceAsStream("/conf/application_local.properties");
        InputStream isQA = this.getClass().getClassLoader().getResourceAsStream("/conf/application_qa.properties");
        InputStream isReal = this.getClass().getClassLoader().getResourceAsStream("/conf/application_real.properties");

        this.servletProp = new Properties();
        try {
            this.servletProp.load(is);

            String e = InetAddress.getLocalHost().getHostAddress();
            String localIP = "127.0.0.1";
            if (localIP.equals(e)) {
                try {
                    boolean e1 = true;
                    Enumeration en = NetworkInterface.getNetworkInterfaces();

                    label79:
                    do {
                        NetworkInterface ni;
                        do {
                            if (!en.hasMoreElements()) {
                                break label79;
                            }

                            ni = (NetworkInterface) en.nextElement();
                        } while (ni.isLoopback());

                        Enumeration inetAddresses = ni.getInetAddresses();

                        while (inetAddresses.hasMoreElements()) {
                            InetAddress ia = (InetAddress) inetAddresses.nextElement();
                            if (ia.getHostAddress() != null && ia.getHostAddress().indexOf(".") != -1) {
                                e = ia.getHostAddress();
                                e1 = false;
                                break;
                            }
                        }
                    } while (e1);
                } catch (SocketException var15) {
                   // log.error(var15.getMessage());
                }
            }

            //log.info(">>>>>>>>>>>>>>>>> Current IP : " + e + " <<<<<<<<<<<<<<<<<<<<<<<<");
            this.setCurrentIP(e);
            if (this.getDevIp() != null && this.getDevIp().indexOf(e) > -1) {
                this.servletProp = null;
                this.servletProp = new Properties();
                this.servletProp.load(isDev);
                this.serverType = SERVER_TYPE.Dev;
            } else if (this.getQAIp() != null && this.getQAIp().indexOf(e) > -1) {
                this.servletProp = null;
                this.servletProp = new Properties();
                this.servletProp.load(isQA);
                this.serverType = SERVER_TYPE.QA;
            } else if ((this.getRealIp() == null || !(this.getRealIp().indexOf(e) > -1) && (this.getRealIp2() == null || !(this.getRealIp2().indexOf(e) > -1)))) {
                this.servletProp = null;
                this.servletProp = new Properties();
                this.servletProp.load(isLocal);
                this.serverType = SERVER_TYPE.Local;
            } else {
                this.servletProp = null;
                this.servletProp = new Properties();
                this.servletProp.load(isReal);
                this.serverType = SERVER_TYPE.Real;
            }

           // log.info(">>>>>>>>>>>>>>>>> Current Server Type : " + this.serverType.toString() + " <<<<<<<<<<<<<<<<<<<<<<<<");
        } catch (IOException var16) {
            //log.error(var16.getMessage());
        }

    }

    public static Common getInstance() {
        if (instance == null) {
            instance = new Common();
        }
        return instance;
    }

    public String getProp(String prop) {
        return this.servletProp.getProperty(prop);
    }

    public String getRootUrl() {
        return this.servletProp.getProperty("context.rootUrl");
    }

    public String getDevIp() {
        return this.servletProp.getProperty("dev.ip");
    }

    public String getQAIp() {
        return this.servletProp.getProperty("qa.ip");
    }

    public String getRealIp() {
        return this.servletProp.getProperty("real.ip");
    }

    public String getRealIp2() {
        return this.servletProp.getProperty("real.ip2");
    }

    //SSO API 연동 신규 추가 kim.dongki
    //SSO_ENC_KEY값
    public String getSSOENCKEY() {
        return this.servletProp.getProperty("SSOENCKEY");
    }

    //SSO siteName 개발/품질
    public String getSiteNameDev() {
        return this.servletProp.getProperty("siteNameDev");
    }

    //SSO siteName 운영
    public String getSiteNameReal() {
        return this.servletProp.getProperty("siteNameReal");
    }

    //SSO authUrl 개발/품질
    public String getAuthUrlDev() {
        return this.servletProp.getProperty("authUrlDev");
    }

    //SSO authUrl 운영
    public String getAuthUrlReal() {
        return this.servletProp.getProperty("authUrlReal");
    }

    public SERVER_TYPE getServerType() {
        return this.serverType;
    }

    public void setServerType(SERVER_TYPE serverType) {
        this.serverType = serverType;
    }

    public String getEncryptAESKey() {
        return this.servletProp.getProperty("encrypt.aes.key");
    }

    public DB_TYPE getDBType() {
        String dbType = this.servletProp.getProperty("jdbc.dbType").toString().toLowerCase();
        if (dbType.equals("mysql")) {
            this.dbType = DB_TYPE.MYSQL;
        } else if (dbType.equals("oracle")) {
            this.dbType = DB_TYPE.ORACLE;
        } else if (dbType.equals("mssql")) {
            this.dbType = DB_TYPE.MSSQL;
        }

        return this.dbType;
    }


    public static enum DB_TYPE {
        MYSQL,
        ORACLE,
        MSSQL;

        private DB_TYPE() {
        }
    }

    public static enum SERVER_TYPE {
        Real,
        Dev,
        QA,
        Local;

        private SERVER_TYPE() {
        }
    }


}
