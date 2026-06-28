package app.mall.dao.dto;

import io.nop.api.core.annotations.data.DataBean;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 单用户算法画像（P20 算法化用户画像 successor）。all-time / 当前快照口径：
 * R=距末单天数、F=累计单数、M=累计消费；RFM 段对 all-time R/F/M 用全量阈值分类；
 * 生命周期阶段按活跃窗口=近 30 天、churn=90 天判定。无消费记录用户 rfmSegment/lifecycleStage 为空、计数为 0。
 * 口径见 docs/design/system-configuration.md「报表与统计」。
 */
@DataBean
public class UserPortraitBean {
    private String userId;
    private long recencyDays;
    private long frequency;
    private BigDecimal monetary;
    private String rfmSegment;
    private String lifecycleStage;
    private Timestamp firstPayTime;
    private Timestamp lastPayTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getRecencyDays() {
        return recencyDays;
    }

    public void setRecencyDays(long recencyDays) {
        this.recencyDays = recencyDays;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public BigDecimal getMonetary() {
        return monetary;
    }

    public void setMonetary(BigDecimal monetary) {
        this.monetary = monetary;
    }

    public String getRfmSegment() {
        return rfmSegment;
    }

    public void setRfmSegment(String rfmSegment) {
        this.rfmSegment = rfmSegment;
    }

    public String getLifecycleStage() {
        return lifecycleStage;
    }

    public void setLifecycleStage(String lifecycleStage) {
        this.lifecycleStage = lifecycleStage;
    }

    public Timestamp getFirstPayTime() {
        return firstPayTime;
    }

    public void setFirstPayTime(Timestamp firstPayTime) {
        this.firstPayTime = firstPayTime;
    }

    public Timestamp getLastPayTime() {
        return lastPayTime;
    }

    public void setLastPayTime(Timestamp lastPayTime) {
        this.lastPayTime = lastPayTime;
    }
}
