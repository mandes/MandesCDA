/* 
 * Copyright 2015 Alexandru Mandes.
 *
 * The code is available under a MIT License.
 *
 * Please cite: Mandes, A. (2015). Microstructure-based order placement in a 
 * continuous double auction agent based model, Algorithmic Finance 4:3-4, 
 * pp. 105-125. DOI: 10.3233/AF-150049. 
 *
 * Further reference: Cui, W. and Brabazon, A. (2012). An agent-based modeling 
 * approach to study price impact, Computational Intelligence for Financial 
 * Engineering & Economics (CIFEr), 2012 IEEE Conference on [proceedings], IEEE Press.
 */
package abm.CuiBrabazonMicro;

import abm.CuiBrabazon.*;
import abmlob.orderbook.Order;
import ccloop.*;

public class CuiMicroTrader extends CuiTrader {
    
    public CuiMicroTrader( MarketState state, int timeFrame, int latency, int cash, int assets, boolean buyTrader ) {
        
        super( state, timeFrame, latency, cash, assets, buyTrader );
        
        this.type = 3;
    }
    
    @Override
    public Order newRandOrder( boolean isBuy, CuiMarketState state, RandNumGen rng ) {
        
        CuiMicroMarketState cmState = (CuiMicroMarketState)state;
        
        int size = (int) Math.floor( rng.nextLogNormal( 8.0, 1.1 ) );
        
        double uC = rng.nextBimodal( cmState.mu1, cmState.sigma1, cmState.mu2, cmState.sigma2, cmState.p1 );
        double stdDev = cmState.mt.instantVola;

        Order ord = cmState.mt.articulateOrder( this, isBuy, size, Benchmarks.EmaPrice, Benchmarks.EmaPrice, uC,
                stdDev, cmState.mt.defaultADV, null );
        
        return ord;
    }

}
