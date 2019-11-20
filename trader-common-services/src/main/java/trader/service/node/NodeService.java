package trader.service.node;

/**
 * 节点服务, 用于主动通过websocket连接节点管理服务
 */
public interface NodeService {
    /**
     * 链接状态
     */
    public static enum ConnectionState{NotConfigured, Connecting, Initialzing, Connected, Disconnected};

    /**
     * 返回本机一致性ID, 格式为: hostName.traderConfigName
     */
    public String getConsistentId();

    /**
     * 返回本机一次性ID
     */
    public String getLocalId();

    public ConnectionState getConnState();

}
