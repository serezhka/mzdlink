# mzdlink
### dev notes
      
##### evtest arm cross compile
    sudo apt-get install gcc-arm-linux-gnueabi
    sudo apt-get install lib6c-dev-i386 lib6c-arm64-cross libc6-dev-armel-cross
    arm-linux-gnueabi-gcc evtest.c -o evtest
