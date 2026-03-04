这是一个汉字手写输入法Demo，通过Google mlkit来实现，并能在识别文字之后联想相似的汉字，并显示在面板中。

CleanableEditText.kt为自定义的EditText。

SecondSurfaceView.kt为自定义的SurfaceView，用于显示手写轨迹。

DigitalInkRecognizeTool.kt为下载语言模型的类。

#笔划识别库

implementation("com.google.mlkit:digital-ink-recognition:18.1.0")

#语言库

implementation("com.google.mlkit:language-id:17.0.5")

#汉字联想，相似汉字相关库

implementation("com.github.houbb:nlp-hanzi-similar:1.4.0")

汉字手写输入法Dmeo运行效果：

![shouxie](https://github.com/user-attachments/assets/753abfa3-e242-4aa6-8aa7-2dac7890b2b7)


