## Lession1

- 概念讲解：安全策略、安全模式、应用程序证书、用户认证和授权、PKI、OPCUA-TCP；

- 应用程序身份
  - __所有OPC UA应用程序都必须定义自己的某些特征。连接应用程序后，此信息将通过OPC UA协议传达给其他应用程序__。
  - __为了进行安全通信，应用程序还必须定义一个应用程序实例证书，它们可以用来向正在与之通信的其他应用程序进行身份验证。根据所选的安全级别，服务器可能只接受来自其信任客户端的连接__。
    - 应用说明(特征)：
      ```
        static String APP_NAME = 'SampleConsoleServer';
        ApplicationDescription appDescription = new ApplicationDescription();
        appDescription.setApplicationName(new LocalizedText(APP_NAME + "@localhost"));
        appDescription.setApplicationUri("urn:localhost:OPCUA:" + APP_NAME);
        appDescription.setProductUri("urn:prosysopc.com:OPCUA:" + APP_NAME);
        appDescription.setApplicationType(ApplicationType.Server);
      ```
      - ApplicationName：在用户界面中用作每个应用程序实例的 __名称标识符__ 。
      - ApplicationUri：是每个正在运行的实例的 __唯一标识符__。
      - ProductUri：用于标识您的产品，因此对于 __所有实例都应该相同__ (引用您自己的域)。
      - 由于标识符对于每个实例都应该是唯一的，因此在ApplicationName和ApplicationUri中都包括其运行所在的计算机的主机名是一个好习惯。
      SDK中将localhost替换为计算机名称。
      - URI必须是有效的标识符，即，它们必须以诸如urn：之类的方案开头，并且不得包含任何空格字符。
      
    - 应用实例证书：
      - 自签名证书
        - 示例中使用ApplicationIdentity.loadOrCreateCertificate()创建自签名证书。
          - 第五个参数(CA certificate & private key, optional)​为null，说明没有使用第三方CA的秘钥对。 
          - SampleConsoleServer@hostname_keysize.der证书(包含公钥)。
            - 证书可以进行公开分发。
          - SampleConsoleServer@hostname_keysize.pem私钥。
            - 从第三方CA获取到的有可能是"*.pfx"，其包含私钥信息。
            - 私钥要保证其存储安全。
            - 私钥支持密码保护，可以通过密码保证其使用安全。
          - hostname被替换为计算机名称；keysize被替换为公钥和私钥的大小。
          
      - 发行人证书 
        - 理想情况下，证书应由公认的证书颁发机构（CA）签名，而不是使用上述自签名密钥。
        - 公认CA签发流程：生成CA机构的秘钥->各应用程序实例使用该秘钥签发自己的证书->使用者使用该秘钥验证应用程序实例证书。
        
      - 多个应用程序实例证书
        - OPC UA规范定义了不同的安全配置文件，这可能需要不同类型的应用程序实例证书，例如具有不同的密钥大小。
    
- 服务发现：
  - 标准端口号:4840;
  - 发现服务器注册是通过安全通道完成的，DiscoveryService需要信任要注册的服务；
    - 内部发现服务器：
        - server自己实现DiscoveryService服务，client通过FindServers获取服务器信息(只能获取到该uaserver信息);
    - 本地发现服务器（LDS）:
        - 保留本地可用的所有服务器列表；
        - 注册到LDS："server.setDiscoveryServerUrl("opc.tcp://localhost:4840");"
    
### Server

- debug initialize函数，讲解安全策略、安全模式、用户认证和授权、PKI等设置过程；

### Client 

- 通过不同安全策略、安全模式、用户认证和授权方式链接server：
  - debug演示client端信息安全、用户认证和授权的处理方式；
  - debug演示server端信息安全、用户认证和授权的处理方式；