To start with your local setup, you need to add below jars in current directory.

└── lib
    ├── commons-codec-1.10.0.wso2v1.jar
    ├── commons-codec-1.10.wso2v1.jar
    ├── commons-compress-1.9.0.wso2v1.jar
    ├── commons-compress-1.9.wso2v1.jar
    ├── dnsjava-2.1.7.wso2v1.jar
    ├── docker-client-1.0.10.wso2v1.jar
    ├── docker-dsl-1.0.10.jar
    ├── docker-model-1.0.10.jar
    ├── dsl-annotations-0.1.26.wso2v1.jar
    ├── fabric8-utils-2.2.100.jar
    ├── jackson-annotations-2.7.3.jar
    ├── jackson-core-2.7.3.jar
    ├── jackson-databind-2.7.3.jar
    ├── jackson-dataformat-yaml-2.7.3.jar
    ├── jaxb-api-2.2.jar
    ├── json-20160212.jar
    ├── junixsocket-common-2.0.4.wso2v1.jar
    ├── kubernetes-client-1.3.76.wso2v1.jar
    ├── kubernetes-model-1.0.43.wso2v1.jar
    ├── logging-interceptor-2.7.2.wso2v1.jar
    ├── mysql-connector-java-5.1.27-bin.jar
    ├── nimbus-jose-jwt_2.26.1.wso2v2.jar
    ├── okhttp-2.7.2.wso2v1.jar
    ├── okhttp-ws-2.7.2.wso2v1.jar
    ├── okio-1.6.0.wso2v1.jar
    ├── org.wso2.carbon.hostobjects.sso_4.2.1.jar
    ├── signedjwt-authenticator_4.3.3.jar
    ├── slf4j-api-1.7.12.wso2v1.jar
    ├── snakeyaml-1.15.jar
    ├── sundr-codegen-0.1.25.wso2v1.jar
    ├── sundr-core-0.1.25.wso2v1.jar
    ├── validation-api-1.1.0.Final.jar
    └── velocity-1.7.0.wso2v1.jar

* If you are using DAS for the Dashboard, you need to install SSO module 1.4.4 from the feature repo http://product-dist.wso2.com/p2/carbon/releases/wilkes to DAS.

* To setup the cluster, first install the HAProxy load balancer as described in the guide [1]. Following is the sample configuration for this HAProxy.

frontend http-in
        bind *:80
        default_backend bk_http

backend bk_http
        balance roundrobin
        server node1 localhost:9763
        server node2 localhost:9767


frontend https-in
        bind *:443 ssl crt /etc/haproxy/ssl/server.pem
        default_backend bk_https

backend bk_https
        balance roundrobin
        server node1 localhost:9443 check ssl verify none
        server node2 localhost:9447 check ssl verify none


1. https://docs.wso2.com/display/CLUSTER420/Configuring+HAProxy

