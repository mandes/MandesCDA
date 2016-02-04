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

import abmlob.events.*;
import abmlob.orderbook.*;
import ccloop.MarketState;
import ccloop.MyException;
import ccloop.TimeStamp;

public class MarketImpactMatchingEngine extends MatchingEngine {
    
    public MarketImpactMatchingEngine( MarketState state, OrderBook orderBook ) {
        
        super( state, orderBook );
    }
    
    @Override
    public void dispatch( Event evt ) {

        TimeStamp operTime = state.clock.getCurTime();
        
        WorkingQuote currentQuote = orderBook.getWorkingQuote( null );
        
        int initOrderSize = 0;
        
        if ( evt.getClass() == EvtSendNewOrder.class ) {

            initOrderSize = ((EvtSendNewOrder)evt).order.outstanding;   // for market impact
            
            if ( validateOrder( ((EvtSendNewOrder)evt).order, operTime ) ) {

                placeNewOrder( ((EvtSendNewOrder)evt).order, operTime, evt );   // returns tracePrice
            }
            // else do nothing
        }
        
        if ( evt.getClass() == EvtModifyOrder.class ) {
            
            modifyOrder( ((EvtModifyOrder)evt).order, operTime, 
                    ((EvtModifyOrder)evt).newLimitPrice, 
                    ((EvtModifyOrder)evt).newOutstanding, evt );   // returns tracePrice
        }
        
        if ( evt.getClass() == EvtRemoveOrder.class ) {
        
            removeOrder( ((EvtRemoveOrder)evt).order, 5, operTime, evt );    // cancel
        }

        if ( evt.getClass() == EvtOrderExpiration.class ) {
        
            removeOrder( ((EvtOrderExpiration)evt).order, 6, operTime, evt );    // expiration
        }
        
        //---- after order processing
        
        WorkingQuote updatedQuote = orderBook.getWorkingQuote( operTime );
        
        if ( !updatedQuote.equals( currentQuote ) ) {

            // best bid/ask change: market order, limit order (crossing, at- or inside-spread), cancel or expire limit order
            
            Quote q = new Quote( orderBook, updatedQuote );

            if ( !state.eventQueue.queue.add( new EvtNotifyQuoteChange( state.eventQueue, operTime, q ) ) ) { // priority 8
                
                throw new MyException("MatchingEngine.dispatch: could not add EvtNotifyQuoteChange to state.eventQueue.queue");
            }
        }
        
        //--- compute price market impact (before/after log mid-quote difference)

        // only within the statistics time-window
        
        if ( operTime.compareTo( ((CuiMarketState)state).burnInPeriod ) == 1 ) {
            
            // only new market orders and valid quotes
            
            if ( evt.getClass() == EvtSendNewOrder.class && !((EvtSendNewOrder)evt).order.isLimit &&
                    currentQuote.bestBid != 0 && currentQuote.bestAsk != 0 && 
                    updatedQuote.bestBid != 0 && updatedQuote.bestAsk != 0 ) {

                // if IOC, the original market order has been transformed into a limit order and thus not taken into consideration

                double mkImp = ( ((EvtSendNewOrder)evt).order.isBuy ? 1 : -1 ) * 
                        Math.log( (double) ( updatedQuote.bestBid + updatedQuote.bestAsk ) / 
                        ( currentQuote.bestBid + currentQuote.bestAsk ) );

                ((CuiMarketState)state).marketImpactSeries.add( 
                        new MarketImpact ( (CuiMarketState)state, initOrderSize, mkImp ) );
            }
        }
    }
}
