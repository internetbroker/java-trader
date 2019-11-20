package trader.service.ta.trend;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import trader.common.exchangeable.Exchange;
import trader.common.exchangeable.Exchangeable;
import trader.common.exchangeable.ExchangeableData;
import trader.common.exchangeable.ExchangeableTradingTimes;
import trader.common.exchangeable.MarketDayUtil;
import trader.common.tick.PriceLevel;
import trader.common.util.DateUtil;
import trader.common.util.TraderHomeUtil;
import trader.service.TraderHomeHelper;
import trader.service.log.LogServiceImpl;
import trader.service.md.MarketData;
import trader.service.md.MarketDataService;
import trader.service.ta.LeveledTimeSeries;
import trader.service.ta.LongNum;
import trader.service.ta.TimeSeriesLoader;
import trader.service.ta.bar.FutureBarBuilder;
import trader.service.util.SimpleBeansContainer;
import trader.simulator.SimMarketDataService;

public class WaveBar2BuilderTest {

    static {
        LogServiceImpl.setLogLevel("org.reflections", "INFO");
        LogServiceImpl.setLogLevel("trader", "INFO");
        LogServiceImpl.setLogLevel("org.apache.commons", "INFO");

        TraderHomeHelper.init(null);
    }

    public void test_ru1901() throws Exception
    {
        SimpleBeansContainer beansContainer = new SimpleBeansContainer();
        final SimMarketDataService mdService = new SimMarketDataService();
        mdService.init(beansContainer);
        beansContainer.addBean(MarketDataService.class, mdService);

        ExchangeableData data = TraderHomeUtil.getExchangeableData();
        TimeSeriesLoader loader= new TimeSeriesLoader(beansContainer, data);

        LocalDate beginDate = DateUtil.str2localdate("20181114");
        LocalDate endDate = DateUtil.str2localdate("20181221");
        LocalDate tradingDay = beginDate;
        Exchangeable e = Exchangeable.fromString("ru1901");

        while(tradingDay.compareTo(endDate)<=0) {
            List<MarketData> ticks = loader.setInstrument(e).loadMarketDataTicks(tradingDay, ExchangeableData.TICK_CTP);
            if ( !ticks.isEmpty() ) {
                PriceLevel level = PriceLevel.resolveVolDaily(ticks.get(0).openInterest, 500);

                ExchangeableTradingTimes tradingTimes = e.exchange().getTradingTimes(e, tradingDay);
                FutureBarBuilder barBuilder = new FutureBarBuilder(tradingTimes, level);

                WaveBarOption option = new WaveBarOption( LongNum.fromRawValue(e.getPriceTick()*4) );

                StackedTrendBar2Builder waveBar2Builder = new StackedTrendBar2Builder(option, tradingTimes, barBuilder);

                for(MarketData tick:ticks) {
                    waveBar2Builder.update(tick);
                }

                LeveledTimeSeries strokeBars = waveBar2Builder.getTimeSeries(PriceLevel.STROKE);
                LeveledTimeSeries sectionBars = waveBar2Builder.getTimeSeries(PriceLevel.SECTION);
                assertTrue(strokeBars.getBarCount()>2);
                if ( strokeBars.getBarCount()>3 ) {
                    assertTrue(sectionBars.getBarCount()>=1);
                }
                for(int i=0;i<sectionBars.getBarCount();i++) {
                    WaveBar bar = (WaveBar)sectionBars.getBar(i);
                    System.out.println(bar);
                    for(Object bar0:bar.getBars()) {
                        System.out.println("\t"+bar0);
                    }
                }

                System.out.println();
            }
            tradingDay = MarketDayUtil.nextMarketDay(Exchange.DCE, tradingDay);
        }
    }

    @Test
    public void test_ru1901_min5() throws Exception
    {
        SimpleBeansContainer beansContainer = new SimpleBeansContainer();
        final SimMarketDataService mdService = new SimMarketDataService();
        mdService.init(beansContainer);
        beansContainer.addBean(MarketDataService.class, mdService);

        ExchangeableData data = TraderHomeUtil.getExchangeableData();
        TimeSeriesLoader loader= new TimeSeriesLoader(beansContainer, data);

        LocalDate beginDate = DateUtil.str2localdate("20181114");
        LocalDate endDate = DateUtil.str2localdate("20181221");
        LocalDate tradingDay = beginDate;
        Exchangeable e = Exchangeable.fromString("ru1901");

        loader.setStartTradingDay(beginDate).setEndTradingDay(endDate).setLevel(PriceLevel.MIN5);

        ExchangeableTradingTimes tradingTimes = e.exchange().getTradingTimes(e, tradingDay);
        FutureBarBuilder barBuilder = new FutureBarBuilder(tradingTimes, PriceLevel.MIN5);
        barBuilder.loadHistoryData(loader);
        WaveBarOption option = new WaveBarOption( LongNum.fromRawValue(e.getPriceTick()*10) );

        StackedTrendBar2Builder waveBar2Builder = new StackedTrendBar2Builder(option, tradingTimes, barBuilder);
        LeveledTimeSeries strokeBars = waveBar2Builder.getTimeSeries(PriceLevel.STROKE);
        LeveledTimeSeries sectionBars = waveBar2Builder.getTimeSeries(PriceLevel.SECTION);
        assertTrue(strokeBars.getBarCount()>2);
        if ( strokeBars.getBarCount()>3 ) {
            assertTrue(sectionBars.getBarCount()>=1);
        }
        for(int i=0;i<sectionBars.getBarCount();i++) {
            WaveBar bar = (WaveBar)sectionBars.getBar(i);
            System.out.println(bar);
            for(Object bar0:bar.getBars()) {
                System.out.println("\t"+bar0);
            }
        }
        System.out.println();
    }

}
