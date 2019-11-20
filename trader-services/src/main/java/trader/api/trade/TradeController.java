package trader.api.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import trader.api.ControllerConstants;
import trader.common.util.JsonUtil;
import trader.service.trade.Account;
import trader.service.trade.Order;
import trader.service.trade.TradeService;

@RestController
public class TradeController {

    private static final String URL_PREFIX = ControllerConstants.URL_PREFIX+"/trade";

    @Autowired
    private TradeService tradeService;

    @RequestMapping(path=URL_PREFIX+"/account",
            method=RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccounts(@RequestParam(name="pretty", required=false) boolean pretty){
        return JsonUtil.json2str(JsonUtil.object2json(tradeService.getAccounts()), pretty);
    }

    @RequestMapping(path=URL_PREFIX+"/account/{accountId}",
            method=RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccount(@PathVariable(value="accountId") String accountId, @RequestParam(name="pretty", required=false) boolean pretty){

        Account account=tradeService.getAccount(accountId);
        if ( null==account) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return JsonUtil.json2str(JsonUtil.object2json(account), pretty);
    }

        @RequestMapping(path=URL_PREFIX+"/account/{accountId}/positions",
        method=RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccountPositions(@PathVariable(value="accountId") String accountId, @RequestParam(name="pretty", required=false) boolean pretty){
        Account account = tradeService.getAccount(accountId);
        if (null == account) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return JsonUtil.json2str(JsonUtil.object2json(account.getPositions()), pretty);
    }

        @RequestMapping(path=URL_PREFIX+"/account/{accountId}/orders",
        method=RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccountOrders(@PathVariable(value="accountId") String accountId, @RequestParam(name="pretty", required=false) boolean pretty){
        Account account = tradeService.getAccount(accountId);
        if (null == account) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return JsonUtil.json2str(JsonUtil.object2json(account.getOrders()), pretty);
    }

        @RequestMapping(path=URL_PREFIX+"/account/{accountId}/order/{orderRef}",
        method=RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAccountOrder(@PathVariable(value="accountId") String accountId, @PathVariable(value="orderRef") String orderRef, @RequestParam(name="pretty", required=false) boolean pretty){
        Account account = tradeService.getAccount(accountId);
        if (null == account) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Order order = account.getOrder(orderRef);
        if ( order==null ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return JsonUtil.json2str(JsonUtil.object2json(order), pretty);
    }

}
