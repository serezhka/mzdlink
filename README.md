# mzdlink

### requirements
* [maven](https://maven.apache.org/download.cgi)
* [java 8](https://java.com/download/)
* [adb](http://forum.xda-developers.com/showthread.php?t=2588979)
* [minicap](https://github.com/openstf/minicap) & [minitouch](https://github.com/openstf/minitouch)
* android device with adb enabled

### running
build minicap & minitouch,
push binaries (minicap, minicap.so, minitouch) to android device /data/local/tmp,
make them executable with chmod 0755

    mvn clean install
    java -jar mzdlink-{version}.jar
    open http://jsfiddle.net/serezhka/x6j1zcde/ (not https to make websockets work)

### dev notes

##### todo
 * try netty-transport-native-epoll
      
##### evtest arm cross compile
    sudo apt-get install gcc-arm-linux-gnueabi
    sudo apt-get install lib6c-dev-i386 lib6c-arm64-cross libc6-dev-armel-cross
    arm-linux-gnueabi-gcc evtest.c -o evtest
