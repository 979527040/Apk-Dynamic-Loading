package mode.dexloaderclass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.newp.IDynamic;

import java.io.File;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class MainActivity extends AppCompatActivity {
    private com.example.newp.IDynamic lib;
    private Button btn_load,btn_dex,btn_path,btn4;
    @Override
    //TODO 准备工作流程，第一步，找到assets中的Dynamic，利用Eclipse把这个类导出为jar，然后进入本地sdk目录下的build-tools目录下的dx.bat工具
    //TODO 第二步，利用dx工具将生成的jar通过指令生成class.dex文件(包含在新生成的jar中)，然后将其拷贝在手机sd卡目录下具体参考印象笔记Dx工具的使用
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_dex= (Button) this.findViewById(R.id.btn1);
        btn_path= (Button) this.findViewById(R.id.btn2);
        btn_load= (Button) this.findViewById(R.id.btn3);
        //TODO 检测权限
        checkPersion();
        btn_dex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 使用DexClassLoader方式加载类
                //TODO dex压缩文件的路径（可以是apk,jar,zip格式）
                String dexPath= Environment.getExternalStorageDirectory().toString()+ File.separator+"dynamic_temp.jar";
                File file=new File(dexPath);
                boolean isExit=file.exists();
                //TODO dex解压释放后的目录,.dex文件在4.1之后是不能保存在sd卡下的,指定dexoutputpath为APP自己的缓存目录即可
                // String dexOutoutDirs=Environment.getExternalStorageDirectory().toString();
                File dexOutputDir = MainActivity.this.getDir("dex", 0);
                //TODO 定义DexClassLoader
                //TODO 第一个参数：是dex压缩文件的路径
                //TODO 第二个参数：是dex解压缩后存放的目录
                //TODO 第三个参数：是C/C++依赖的本地库文件目录,可以为null
                //TODO 第四个参数：是上一级的类加载器
                DexClassLoader c1=new DexClassLoader(dexPath,dexOutputDir.getAbsolutePath(),null,getClassLoader());
                try {
                    loadUtil(c1);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 使用PathClassLoader方法加载类
                //TODO 创建一个意图，用来找到指定的apk：这里的"com.dynamic.impl是指定apk中在AndroidMainfest.xml文件中定义的
                Intent intent=new Intent("com.dynamic.impl",null);
                //TODO 获得包管理器
                PackageManager pm=getPackageManager();
                List<ResolveInfo> resolveInfoList=pm.queryIntentActivities(intent,0);
                //TODO 获取的指定的activity信息
                ActivityInfo activityInfo=resolveInfoList.get(0).activityInfo;
                //TODO 获得apk的目录或者jar的目录
//                String apkPath=activityInfo.applicationInfo.sourceDir;
                String dexPath= Environment.getExternalStorageDirectory().toString()+ File.separator+"dynamic_temp.jar";
                //TODO native代码的目录
                String libPath=activityInfo.applicationInfo.nativeLibraryDir;
                //TODO 创建类加载器把dex加载到虚拟机中
                //TODO 第一个参数：是指定apk安装的路径，这个路径要注意只能是通过actInfo.applicationInfo.sourceDir来获取
                //TODO 第二个参数：是C/C++依赖的本地库文件目录,可以为null
                //TODO 第三个参数：是上一级的类加载器

                PathClassLoader pc1=new PathClassLoader(dexPath,libPath,getClassLoader());
                //TODO 加载类
                //TODO com.example.newp.Dynamic是动态类名
                try {
                    loadUtil(pc1);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        //TODO 分别调用动态类中的方法
        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lib!=null){
                    lib.showDialog();
                }else{
                    Toast.makeText(getApplicationContext(), "类加载失败", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    //TODO 动态加载
    //TODO com.example.newp.Dynamic是被本地Dx.bat生成的dynamic_temp.jar中的class.dex中的Dynamic.class的包名和类名
    private void loadUtil(Object classLoader) throws ClassNotFoundException {
        DexClassLoader dx = null;
        PathClassLoader pc=null;
        Class libProviderClazz = null;
        if(classLoader instanceof DexClassLoader){
            dx= (DexClassLoader) classLoader;
            libProviderClazz=dx.loadClass("com.example.newp.Dynamic");
        }else if(classLoader instanceof PathClassLoader){
            pc= (PathClassLoader) classLoader;
            libProviderClazz=pc.loadClass("com.example.newp.Dynamic");
        }
        try {

            lib=(IDynamic)libProviderClazz.newInstance();
            if(lib!=null){
                lib.init(MainActivity.this);
            }} catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkPersion(){
        //TODO 检查权限
        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {*/
            //进入到这里代表没有权限.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
       /* } else {
            Toast.makeText(this,"已经申请到权限",Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:
                if(grantResults.length >0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //TODO 用户同意授权
                    Toast.makeText(this,"同意授权",Toast.LENGTH_LONG).show();
                }else{
                    //TODO 用户拒绝授权
                }
                break;
        }
    }
}
