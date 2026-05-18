# USBテザリング ワンタップアプリ (Android 9)

タップするだけでUSBテザリングをオン/オフできるAndroidアプリです。

---

## 📁 プロジェクト構成

```
UsbTetheringApp/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/usbtetheringapp/
│   │   │   └── MainActivity.kt        ← メインロジック
│   │   └── res/
│   │       ├── layout/activity_main.xml
│   │       ├── drawable/button_rounded.xml
│   │       └── values/{colors,strings,styles}.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🔧 ビルド方法

1. **Android Studio** (Arctic Fox 以降) でプロジェクトを開く
2. `Build > Make Project` でビルド
3. デバイスまたはエミュレータで `Run` を実行

または Gradle コマンドライン：
```bash
./gradlew assembleDebug
```
APK は `app/build/outputs/apk/debug/` に生成されます。

---

## ⚙️ 動作の仕組み

### リフレクション方式 (メイン)

Android の `ConnectivityManager` には隠しメソッド `setUsbTethering(boolean)` があります。
本アプリはリフレクションでこれを呼び出します：

```kotlin
val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
val method = cm.javaClass.getDeclaredMethod("setUsbTethering", Boolean::class.java)
method.isAccessible = true
method.invoke(cm, true) // true = ON, false = OFF
```

### フォールバック方式

直接制御できない場合は、テザリング設定画面を直接起動します：
```kotlin
Intent().setClassName("com.android.settings", "com.android.settings.TetherSettings")
```

---

## ⚠️ 制限事項・注意

| 状況 | 動作 |
|------|------|
| 通常のAndroid 9端末 | 設定画面へのショートカットとして機能 |
| root化済み端末 | ワンタップで直接切り替え可能 |
| カスタムROM (LineageOS等) | 直接切り替えできる場合あり |
| メーカーカスタマイズ端末 | 挙動が異なる場合あり |

### Android 8以降の制限について

Android 8 (Oreo) 以降、`TETHER_PRIVILEGED` というsignatureレベルの権限が
テザリングの直接制御に必要になりました。
一般アプリにはこの権限が付与されないため、root化なしの端末では
直接制御できない場合があります。

### root化端末での完全動作

root化済み端末では以下のコマンドを shell 経由で実行することも可能です：
```
svc usb setFunctions rndis  # ON
svc usb setFunctions        # OFF
```

---

## 🔌 使い方

1. PCと端末をUSBケーブルで接続
2. アプリを起動
3. 「タップしてオンにする」ボタンを押す
4. 直接切り替えできた場合 → テザリング開始
5. できなかった場合 → 設定画面が開くので手動で有効化

---

## 必要環境

- Android 8.0 (API 26) 以上
- Android 9 (API 28) で動作確認
- Android Studio / Gradle でビルド可能
