package binace.trade;

import binace.config.BinaceConfig;
import com.binance.connector.client.impl.SpotClientImpl;

public class BinaceClient {

    public static final SpotClientImpl client = new SpotClientImpl(
        BinaceConfig.API_KEY
        , BinaceConfig.SECRET_KEY
        , BinaceConfig.BASE_Url);
}
