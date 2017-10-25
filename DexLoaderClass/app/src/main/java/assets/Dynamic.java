package assets;

/**
 * Created by Administrator on 2017/10/25.
 */
import android.app.Activity;
import android.widget.Toast;

public class Dynamic implements IDynamic {
    private Activity mActivity;

    public Dynamic() {
    }

    @Override
    public void init(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Override
    public void showBanner() {
        Toast.makeText(this.mActivity, "我是ShowBannber方法", 1500).show();
    }

    @Override
    public void showDialog() {
        Toast.makeText(this.mActivity, "我是showDialog方法", 1500).show();
    }

    @Override
    public void showFullScreen() {
        Toast.makeText(this.mActivity, "我是showFullScreen方法", 1500).show();
    }

    @Override
    public void showAppWall() {
        Toast.makeText(this.mActivity, "我是showAppWall方法", 1500).show();
    }

    @Override
    public void destroy() {
        Toast.makeText(this.mActivity, "我是destroy方法", 1500).show();
    }
}
