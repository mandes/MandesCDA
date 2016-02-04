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
package abm.CuiBrabazon;

import abmlob.events.Event;
import abmlob.events.EvtNotifyTrade;
import abmlob.events.Handler;
import abmlob.orderbook.Trade;
import ccloop.Consts;
import ccloop.MarketState;

public class HdlCuiNotifyTrade extends Handler {
    
    @Override
    public void broadcast( Event evt, MarketState state ) {

        CuiMarketState cuiState = (CuiMarketState)state;
        
        Trade t = ((EvtNotifyTrade)evt).trade;
        
        double percRet;

        if ( cuiState.tradeHistory.isEmpty() ) {
            
            percRet = ( (double) t.price / Consts.NULLPRICE - 1.0 ) * 100;
        }
        else {
            
            percRet = ( (double) t.price / cuiState.tradeHistory.peekLast().price - 1.0 ) * 100;
        }

        //---- only after the burn-in period
        
        if ( state.clock.getCurTime().compareTo( cuiState.burnInPeriod ) == 1 ) {
            
            cuiState.tradeCnt++;
            cuiState.tradeVol += t.size;
            
            cuiState.sumRet += percRet;
            cuiState.sqSumRet += Math.pow(percRet, 2);
        }
        
        //----- store in MarketState
        
        state.addTrade( ((EvtNotifyTrade)evt).trade );
    }
}