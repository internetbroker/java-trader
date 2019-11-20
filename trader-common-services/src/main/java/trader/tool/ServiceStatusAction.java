package trader.tool;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import trader.common.beans.BeansContainer;
import trader.common.util.ConversionUtil;
import trader.common.util.IniFile;
import trader.common.util.StringUtil.KVPair;
import trader.common.util.SystemUtil;
import trader.service.util.CmdAction;

public class ServiceStatusAction implements CmdAction {

    @Override
    public String getCommand() {
        return "service.status";
    }

    @Override
    public void usage(PrintWriter writer) {
        writer.println("service status");
        writer.println("\t服务运行状态");
    }

    @Override
    public int execute(BeansContainer beansContainer, PrintWriter writer, List<KVPair> options) throws Exception
    {
        File statusFile = ServiceStartAction.getStatusFile();
        IniFile statusIni = null;
        if ( statusFile.exists() && statusFile.length()>0 ) {
            statusIni = new IniFile(statusFile);
            IniFile.Section startSection = statusIni.getSection("start");
            IniFile.Section readySection = statusIni.getSection("ready");
            String status = null;
            long pid = ConversionUtil.toLong( startSection.get("pid") );
            if ( !SystemUtil.isProcessPresent(pid) ) {
                status = ""+pid+" Closed";
            }else {
                if ( readySection==null ) {
                    status = ""+pid+" Starting";
                }else {
                    status = ""+pid+" Started";
                }
            }

            writer.println("Status: "+status);
            writer.println("Start time: "+startSection.get("startTime"));
        }
        return 0;
    }

}
