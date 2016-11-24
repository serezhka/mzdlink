# mzdlink
### dev notes
##### minicap's run.sh new line characters problem
    Error:
      run.sh: line 2: $'\r': command not found
      
    Solution:
      sed -i 's/\r$//' run.sh
      
##### minicap's run.sh permission denied
    Error:
      /system/bin/sh: /data/local/tmp/minicap-devel/minicap: can't execute: Permission denied
      
    Solution:
      Edit run.sh 
      ...
      # Upload the binary
      adb push libs/$abi/$bin $dir
      add adb shell chmod 0755 $dir/$bin # <-- add this line
    
##### minicap's example nodejs module not found
    Error:
      Cannot find module 'ws' (and|or espress)
      
    Solution:
      * npm install --save ws
      * npm install express
      
##### evtest arm cross compile
    sudo apt-get install gcc-arm-linux-gnueabi
    sudo apt-get install lib6c-dev-i386 lib6c-arm64-cross libc6-dev-armel-cross
    arm-linux-gnueabi-gcc evtest.c -o evtest
