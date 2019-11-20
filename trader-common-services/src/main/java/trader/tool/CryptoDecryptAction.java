package trader.tool;

import java.io.PrintWriter;
import java.util.List;

import trader.common.beans.BeansContainer;
import trader.common.util.EncryptionUtil;
import trader.common.util.StringUtil;
import trader.common.util.StringUtil.KVPair;
import trader.service.util.CmdAction;

public class CryptoDecryptAction implements CmdAction {

    @Override
    public String getCommand() {
        return "crypto.decrypt";
    }

    @Override
    public void usage(PrintWriter writer) {
        writer.println("crypto decrypt <SECRETE_TEXT>");
        writer.println("\t解密文本");
    }

    @Override
    public int execute(BeansContainer beansContainer, PrintWriter writer, List<KVPair> options) throws Exception {
        String secretText = options.get(0).str;
        String result = new String(EncryptionUtil.symmetricDecrypt(secretText), StringUtil.UTF8);
        writer.println(result);
        return 0;
    }

}
