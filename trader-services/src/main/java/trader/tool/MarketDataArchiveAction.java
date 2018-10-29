package trader.tool;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import trader.common.exchangeable.Exchangeable;
import trader.common.exchangeable.ExchangeableData;
import trader.common.exchangeable.ExchangeableDataArchiveListener;
import trader.common.util.TraderHomeUtil;

public class MarketDataArchiveAction implements CmdAction, ExchangeableDataArchiveListener {

    PrintWriter writer;

    @Override
    public String getCommand() {
        return "marketData.archive";
    }

    @Override
    public void usage(PrintWriter writer) {
        writer.println("marketData archive");
        writer.println("\t压缩已导入的行情数据");
    }

    @Override
    public int execute(PrintWriter writer, List<String> options) throws Exception {
        File dataDir = new File(TraderHomeUtil.getTraderHome(), "data");
        ExchangeableData exchangeableData = new ExchangeableData(dataDir, false);
        this.writer = writer;
        exchangeableData.archive(this);
        return 0;
    }

    @Override
    public void onArchiveBegin(Exchangeable e, File edir) {
        writer.print("归档 "+e+" 目录: "+edir+" ... "); writer.flush();
    }

    @Override
    public void onArchiveEnd(Exchangeable e, int archivedFileCount) {
        writer.println("完成("+archivedFileCount+")"); writer.flush();
    }

    @Override
    public void onArchiveBegin(File subDir) {
    }

    @Override
    public void onArchiveEnd(File subDir, int archivedFileCount) {
    }

}