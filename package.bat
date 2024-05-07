:: mvn dependency:resolve -U 重新下载依赖包，尤其针对java_utilbox
:: mvn clean package 重新打包构建
mvn dependency:resolve -U && mvn clean package