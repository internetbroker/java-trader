package trader.service.ta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.Num;

import trader.common.exchangeable.Exchange;
import trader.common.exchangeable.Exchangeable;
import trader.common.exchangeable.ExchangeableTradingTimes;
import trader.common.exchangeable.MarketDayUtil;
import trader.common.util.PriceUtil;
import trader.service.trade.TradeConstants.PosDirection;

/**
 * Bar的一些辅助函数
 */
public class BarHelper {

    /**
     * 从后向前找最高价的Bar
     *
     * @param series 序列
     * @param endIdx 最后一个包含的Bar index(包含), -1 代表最后一个
     * @param lastN 最后N个Bar中找
     */
    public static int lastHighest(TimeSeries series, int endIdx, int lastN) {
        if ( endIdx<0 ) {
            endIdx = series.getBarCount() - 1;
        }
        int beginIdx = endIdx - lastN-1;
        if (beginIdx<0) {
            beginIdx = 0;
        }
        Bar maxBar = series.getBar(beginIdx);
        int result = beginIdx;
        for(int i=beginIdx; i<=endIdx; i++) {
            Bar bar0 = series.getBar(i);
            if ( bar0.getMaxPrice().isGreaterThan(maxBar.getMaxPrice())) {
                maxBar = bar0;
                result = i;
            }
        }
        return result;
    }

    /**
     * 从后向前找最低价的Bar
     *
     * @param series 序列
     * @param endIdx 最后一个包含的Bar index(包含), -1 代表最后一个
     * @param lastN 最后N个Bar中找
     *
     * @return
     */
    public static int lastLowest(LeveledTimeSeries series, int endIdx, int lastN) {
        if ( endIdx<0 ) {
            endIdx = series.getBarCount() - 1;
        }
        int beginIdx = endIdx - lastN-1;
        if (beginIdx<0) {
            beginIdx = 0;
        }
        Bar minBar = series.getBar(beginIdx);
        int result = beginIdx;
        for(int i=beginIdx; i<=endIdx; i++) {
            Bar bar0 = series.getBar(i);
            if ( bar0.getMinPrice().isLessThan(minBar.getMinPrice())) {
                minBar = bar0;
                result = i;
            }
        }
        return result;
    }

    /**
     * 返回两个Bar之间的市场时间, 单位毫秒
     */
    public static long getBarsDuration(Bar2 bar, Bar2 bar2) {

        ExchangeableTradingTimes tradingTimes = bar.getTradingTimes();
        ExchangeableTradingTimes tradingTimes2 = bar2.getTradingTimes();
        Exchangeable instrument = tradingTimes.getInstrument();
        Exchange exchange = tradingTimes.getInstrument().exchange();

        LocalDateTime beginTime = bar.getEndTime().toLocalDateTime();
        LocalDateTime endTime = bar2.getBeginTime().toLocalDateTime();

        long result = 0;
        long endMktMillis = tradingTimes.getTradingTime(endTime);
        long beginMktMillis = tradingTimes.getTradingTime(beginTime);
        if( tradingTimes.getTradingDay().equals(tradingTimes2.getTradingDay())) {
            //相同交易日
            result = Math.abs(endMktMillis-beginMktMillis);
        } else {
            //隔日, 第一天计算从第一个Bar到收市
            result = (tradingTimes.getTotalTradingMillis()-beginMktMillis);
            //计算整天
            LocalDate tradingDay = MarketDayUtil.nextMarketDay(exchange, tradingTimes.getTradingDay());
            while(!tradingDay.equals(tradingTimes2.getTradingDay())) {
                ExchangeableTradingTimes currTradingTimes = exchange.getTradingTimes(instrument, tradingDay);
                result += (currTradingTimes.getTotalTradingMillis());
            }
            //计算最后一个Bar
            result += (endMktMillis);
        }

        return result;
    }

    /**
     * KBar 存在重叠
     */
    public static boolean barOverlaps(Bar b, Bar b2) {
        Num mmax = b.getMaxPrice().max(b2.getMaxPrice());
        Num mmin = b.getMinPrice().min(b2.getMinPrice());

        Num mheight = mmax.minus(mmin);
        Num height = b.getMaxPrice().minus(b.getMinPrice());
        Num height2 = b2.getMaxPrice().minus(b2.getMinPrice());

        boolean result = false;
        if ( mheight.isLessThan(height.plus(height2)) ){
            if ( height.isGreaterThan(height2) ) {
                //b比b2高, 检查中心
                Num bcenter = b.getMaxPrice().plus(b.getMinPrice()).dividedBy(LongNum.TWO);
                result = b2.getMaxPrice().isGreaterThanOrEqual(bcenter) && b2.getMinPrice().isLessThanOrEqual(bcenter);
            } else {
                //b2 比 b高, 检查中心
                Num b2center = b2.getMaxPrice().plus(b2.getMinPrice()).dividedBy(LongNum.TWO);
                result = b.getMaxPrice().isGreaterThanOrEqual(b2center) && b.getMinPrice().isLessThanOrEqual(b2center);
            }
        }
        return result;
    }

    public static long getBarHeight(Bar bar) {
        Num max = bar.getMaxPrice(), min = bar.getMinPrice();
        return PriceUtil.num2long(max.minus(min));
    }

    public static long max2close(Bar bar) {
        Num max = bar.getMaxPrice(), close = bar.getClosePrice();
        return PriceUtil.num2long(max.minus(close));
    }

    public static long min2close(Bar bar) {
        Num min = bar.getMinPrice(), close = bar.getClosePrice();
        return PriceUtil.num2long(close.minus(min));
    }

    public static List<Bar2> series2bars(TimeSeries series){
        List<Bar2> result = new ArrayList<>(series.getBarCount());
        for(int i=0;i<series.getBarCount();i++) {
            result.add( (Bar2)series.getBar(i) );
        }
        return result;
    }

    public static PosDirection getDirection(Bar bar) {
        Num o=bar.getOpenPrice(), c=bar.getClosePrice();
        if ( o.isGreaterThan(c)) {
            return PosDirection.Long;
        } else if ( o.isLessThan(c) ) {
            return PosDirection.Short;
        } else {
            Num h=bar.getMaxPrice(), l=bar.getMinPrice();
            Num oh = h.minus(o), ol = o.minus(l);
            if ( oh.isGreaterThanOrEqual(ol)) {
                return PosDirection.Long;
            }  {
                return PosDirection.Short;
            }
        }
    }

}
