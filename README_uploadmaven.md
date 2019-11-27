# DID.Java.SDK

## upload to maven
```
./gradlew clean build bintrayUpload -PbintrayUser=xxx -PbintrayKey=xxx -PdryRun=false  -x test -x javadoc
```

-x test: skip test

-x javadoc: skip javadoc