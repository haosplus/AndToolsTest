扩展基于robotium的测试框架, 重写InstrumentationTestRunner实现报告输出、android aap形式管理和执行case，解决权限操作等问题；

AndToolsTest：
## 配置： ##
当需要针对自己的APK或者工程进行测试的时候，修改AndToolsTest工程中的AndroidManifest.xml

```
    <instrumentation
        android:name="com.auto.tools.instrumentation.AndToolsInstrumentationTestRunner"
        android:label="AndToolsTest"
        android:targetPackage="com.example.android.actionbarcompat" />

```
将**android:targetPackage**修改成自己测试对象的包名即可；

## 指定执行case ##
编辑AndToolsTest工程assets目录下面的testcases.json文件：

```
[
    {
        "class": "com.auto.test.SmokeMAutoTest",
        "testsuite": [
            {
                "name": "testDemo",
                "description": "测试示例",
                "isrun": "false"
            }
        ]
    },
    {
        "class": "com.auto.test.SmokeAutoTest",
        "testsuite": [
            {
                "name": "testDebug",
                "description": "调试专用",
                "isrun": "true"
            }
        ]
    }
]


```
##  管理界面： ##
![image](https://github.com/hao-shen/AndToolsTest/blob/master/images/Screenshot_2016-11-28-19-49-17.png)
