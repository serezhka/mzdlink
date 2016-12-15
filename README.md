# mzdlink

### requirements
* [maven](https://maven.apache.org/download.cgi)
* [java 8](https://java.com/download/)
* [adb](http://forum.xda-developers.com/showthread.php?t=2588979)
* android device with adb enabled

### running
    mvn clean install
    java -jar mzdlink-{version}.jar
    open http://jsfiddle.net/serezhka/x6j1zcde/ (not https to make websockets work)

### dev notes
      
##### evtest arm cross compile
    sudo apt-get install gcc-arm-linux-gnueabi
    sudo apt-get install lib6c-dev-i386 lib6c-arm64-cross libc6-dev-armel-cross
    arm-linux-gnueabi-gcc evtest.c -o evtest
