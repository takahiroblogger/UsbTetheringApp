# ConnectivityManager の隠し API がリフレクションで参照できるよう保持
-keep class android.net.ConnectivityManager {
    public *;
}
-keepclassmembers class android.net.ConnectivityManager {
    public *;
}
