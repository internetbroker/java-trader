package trader.service.trade;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import trader.common.exchangeable.Exchange;
import trader.common.exchangeable.ExchangeableTradingTimes;

/**
 * 交易时间服务
 */
@Service
public class MarketTimeServiceImpl implements MarketTimeService {

    private LocalDate tradingDay;

    @PostConstruct
    public void init() {
        ExchangeableTradingTimes tradingTimes = Exchange.SHFE.detectTradingTimes("au", LocalDateTime.now());
        if ( tradingTimes!=null ) {
            tradingDay = tradingTimes.getTradingDay();
        }
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 返回市场时间
     */
    @Override
    public LocalDateTime getMarketTime() {
        return LocalDateTime.now();
    }

    /**
     * 返回市场当天时间
     */
    @Override
    public LocalDate getTradingDay() {
        return tradingDay;
    }

}
