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
import abmlob.events.EvtSendNewOrder;
import abmlob.events.Handler;
import abmlob.orderbook.Order;
import abmlob.orderbook.WorkingQuote;
import ccloop.MarketState;

public class HdlCuiSendNewOrder extends Handler {
    
    @Override
    public void broadcast( Event evt, MarketState state ) {
      
        //---- clasify order and update counts
        
        // only after the burn in period
        
        if ( state.clock.getCurTime().compareTo( ((CuiMarketState)state).burnInPeriod ) == 1 ) {

            CuiMarketState s = (CuiMarketState)state;
            WorkingQuote q = s.matchingEngine.orderBook.getBidAskSpread( null );

            Order o = ((EvtSendNewOrder)evt).order;

            if ( o.agent.getClass() == CuiMarketMaker.class ) {

                s.mmOrdCnt++;
            }
            else {  // no market maker

                if ( !o.isLimit ) { // market

                    s.mkOrdCnt++;
                    
                    int availableDepth;
                    
                    if ( o.isBuy ) {
                        
                        availableDepth = s.matchingEngine.orderBook.getBookDepth( false );
                    }
                    else {  // sell
                        
                        availableDepth = s.matchingEngine.orderBook.getBookDepth( true );
                    }
                    
                    if ( o.outstanding >= availableDepth ) {
                        
                        s.iocOrders++;
                    }
                }
                else {  // limit order

                    if ( o.isBuy ) {

                        if ( o.limitPrice >= q.bestAsk ) {
                            
                            s.effCrossLimOrdCnt++;
                        }
                        else {
                            
                            if ( o.limitPrice > q.bestBid ) {

                                s.effInSprLimOrdCnt++;
                            }
                            else {

                                if ( o.limitPrice == q.bestBid ) {
                                    
                                    s.effSprLimOrdCnt++;
                                }
                                else {  // o.limitPrice < q.bestBid
                                    
                                    s.effOffSprLimOrdCnt++;

                                    s.relLimDistHistory.add(
                                        new OffSpreadRelLimDist( s.clock.getCurTime(), q.bestBid - o.limitPrice ) );
                                }
                            }
                        }
                    }
                    else {  // sell

                        if ( o.limitPrice <= q.bestBid ) {
                            
                            s.effCrossLimOrdCnt++;
                        }
                        else {
                            
                            if ( o.limitPrice < q.bestAsk ) {

                                s.effInSprLimOrdCnt++;
                            }
                            else {
                            
                                if ( o.limitPrice == q.bestAsk ) {
                                    
                                    s.effSprLimOrdCnt++;
                                }
                                else {  // o.limitPrice > q.bestAsk
                                    
                                    s.effOffSprLimOrdCnt++;

                                    s.relLimDistHistory.add(
                                        new OffSpreadRelLimDist( s.clock.getCurTime(), o.limitPrice - q.bestAsk ) );
                                }
                            }
                        }                    
                    }
                }
            }
        }
        
        //---- send instruction to market
        
        state.matchingEngine.dispatch(evt);
    }
}