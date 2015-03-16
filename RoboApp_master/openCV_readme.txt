This is the instruction for setting up NDK for openCV
download OpenCV-2.4.10-android-sdk from the downloads section at first, then:

1. download NDK from https://developer.android.com/tools/sdk/ndk/index.html
   unzip it and be sure the path to the NDK does NOT contain any space
2. creat a file "grade.properties" at the root of the project folder, if you don't have one
    add the NDK path into the file 
    such like "ndkDir=C\:\\Android\\ndk" in windows
    (change the path to your own path)
3. add the NDK path into local.properties
4. in the file "src/main/jni/Android.mk"
    change the line that contian the path to OpenCV.mk,
    which should like "include C:\Android\OpenCV-2.4.10-android-sdk\sdk\native\jni\OpenCV.mk",
    to your own path of OpenCV.mk