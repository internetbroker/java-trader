package trader.service.ta;

import java.util.List;

import trader.common.beans.Lifecycle;
import trader.common.exchangeable.Exchangeable;

/**
 * 技术分析/KBar服务
 * <BR>由于KBar的维护耗时极短, 因此放在行情的回调线程中直接完成, 不需要单独线程处理
 */
public interface TechnicalAnalysisService extends Lifecycle {

    /**
     * 某个品种的所有的技术分析的KBAR, 指标等等
     */
    public TechnicalAnalysisAccess forInstrument(Exchangeable instrument);

    /**
     * 为指定的品种的特定级别的KBar增加Listener
     */
    public void registerListener(List<Exchangeable> instruments, TechnicalAnalysisListener listener);

}
