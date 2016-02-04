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
import abmlob.events.EvtNotifyQuoteChange;
import abmlob.events.Handler;
import abmlob.orderbook.Quote;
import ccloop.MarketState;

public class HdlCuiNotifyQuoteChange extends Handler {
 
    @Override
    public void broadcast( Event evt, MarketState state ) {

        //---- after burn-in period
        
        if ( state.clock.getCurTime().compareTo( ((CuiMarketState)state).burnInPeriod ) == 1 ) {
            
            CuiMarketState s = (CuiMarketState)state;
            
            Quote q = ((EvtNotifyQuoteChange)evt).quote;
            
            if ( q.bestBid != 0 && q.bestAsk != 0 ) {
                
                s.avSpread = ( s.avSpread * s.quoteCount + q.bestAsk - q.bestBid ) / ( s.quoteCount + 1 );
                
                double percSpread = 2.0 * ( q.bestAsk - q.bestBid ) * 100 / ( q.bestBid + q.bestAsk);
                
                s.avPercSpread = ( s.avPercSpread * s.quoteCount + percSpread ) / ( s.quoteCount + 1 );
            
                s.quoteCount++;
            }
        }
        
        //---- store in MarketState
        
        state.addQuote( ((EvtNotifyQuoteChange)evt).quote );
    }
}
