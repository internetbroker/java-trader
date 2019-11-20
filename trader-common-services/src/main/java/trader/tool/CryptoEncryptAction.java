package trader.tool;

import java.io.PrintWriter;
import java.util.List;

import trader.common.beans.BeansContainer;
import trader.common.util.EncryptionUtil;
import trader.common.util.StringUtil;
import trader.common.util.StringUtil.KVPair;
import trader.service.util.CmdAction;

/**
 * 解密
 */
public class CryptoEncryptAction implements CmdAction {

    @Override
    public String getCommand() {
        return "crypto.encrypt";
    }

    @Override
    public void usage(PrintWriter writer) {
        writer.println("crypto encrypt <PLAIN_TEXT>");
        writer.println("\t加密文本");
    }

    @Override
    public int execute(BeansContainer beansContainer, PrintWriter writer, List<KVPair> options) throws Exception {
        String plainText = options.get(0).str;
        String result = EncryptionUtil.symmetricEncrypt(plainText.getBytes(StringUtil.UTF8));
        writer.println(result);
        return 0;
    }

}
