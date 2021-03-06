package com.runhang.shadow.client.core.sync.callback;

import com.runhang.shadow.client.core.bean.shadow.ShadowBean;
import com.runhang.shadow.client.core.bean.shadow.ShadowDesiredDoc;
import com.runhang.shadow.client.core.bean.shadow.ShadowDoc;
import com.runhang.shadow.client.core.bean.comm.ShadowOpsBean;
import com.runhang.shadow.client.core.enums.ReErrorCode;
import com.runhang.shadow.client.core.exception.NoSriException;
import com.runhang.shadow.client.core.exception.NoTopicException;
import com.runhang.shadow.client.core.sync.push.UpdateReplyPush;

/**
 * @ClassName ShadowUpdateCallback
 * @Description 更新影子的回调
 * @Date 2019/4/29 16:39
 * @author szh
 **/
public class ShadowUpdateCallback extends AbsMqttCallback {

    private UpdateReplyPush updateReplyPush;

    public ShadowUpdateCallback() {
        this.updateReplyPush = new UpdateReplyPush();
    }

    @Override
    public void dealMessage(ShadowOpsBean opsBean, ShadowBean shadowBean) throws NoTopicException, NoSriException {
        int deviceVer = opsBean.getVersion();
        int shadowVer = shadowBean.getDoc().getVersion();

        if (deviceVer > 0 && deviceVer <= shadowVer) { // 版本小于1时强制更新
            // 推送版本错误信息
            updateReplyPush.pushError(shadowBean.getTopic(), ReErrorCode.VERSION_CONFLICT);
            return;
        }

        ShadowDoc shadowDoc = shadowBean.getDoc();
        // 设备状态更新成功，清除desired
        if (null == opsBean.getState().getDesired()) {
            ReErrorCode error = shadowBean.clearDesired();
            if (null != error) {
                // 推送写锁错误
                updateReplyPush.pushError(shadowBean.getTopic(), error);
                return;
            }
        }
        // 更新影子
        ShadowDesiredDoc reportedValue = opsBean.getState().getReported();
        if (null != reportedValue) {
            // 更新影子属性
            ReErrorCode error = shadowBean.updateShadowByDevice(reportedValue);
            if (null != error) {
                // 推送属性错误信息
                updateReplyPush.pushError(shadowBean.getTopic(), error);
                return;
            }
        }
        // 更新版本号
        int nextVer = deviceVer > 0 ? deviceVer : shadowVer + 1;
        shadowDoc.setVersion(nextVer);
        // 推送成功信息
        updateReplyPush.pushSuccess(shadowBean.getTopic(), nextVer);
    }

}
