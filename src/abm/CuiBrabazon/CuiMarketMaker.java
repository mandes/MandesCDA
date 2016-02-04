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

import abmlob.agents.Trader;
import abmlob.events.Event;
import abmlob.events.EvtEmptyBook;
import abmlob.events.EvtSendNewOrder;
import abmlob.orderbook.Order;
import ccloop.Consts;
import ccloop.MarketState;
import ccloop.MyException;
import ccloop.RandNumGen;

public class CuiMarketMaker extends Trader {
    
    public int defaultSpread = 50;
            
    public CuiMarketMaker( MarketState state, int timeFrame, int latency, int cash, int assets ) {
        
        super( state, timeFrame, 0, latency, cash, assets );
        
        this.type = 2;
    }
    
    @Override
    public void dispatch( Event evt, MarketState state ) {
    
        if ( evt.getClass() == EvtEmptyBook.class ) {
            
            for (int i = 0; i < 3; i++) {   // three buy limit orders

                Event tradeEvent = this.generateOrder( ((EvtEmptyBook)evt).bidSide, state, state.rng );

                if ( tradeEvent != null ) {

                    state.eventQueue.queue.add( tradeEvent );
                }
            }
        }
    }
    
    @Override
    public Event nextWakeUp( MarketState state, RandNumGen rng ) {
        
        return null;
    }
    
    @Override
    public Event trade( MarketState state, RandNumGen rng ) {
     
        return null;
    }

    public Event generateOrder( boolean buyOrder, MarketState state, RandNumGen rng ) {

        // off-spread limit order size -\mu = 8.2166,\sigma = 0.9545
        int size = (int) Math.floor( rng.nextLogNormal(8.2166, 0.9545) );
        
        int limPrice;
        
        // off-spread relative limit price - xmin = 0.05, \beta = 1.7248
        int delta = (int) Math.round( rng.nextCuiPowerLaw( 1.7248, 0.05 ) * Math.pow( 10, Consts.PRICEDIGITS ) );

        if ( delta < 0 ) {

            throw new MyException("CuiMarketMaker.tradeSide - delta should be positive");    
        }

        if ( buyOrder ) {
            
            if ( state.orderBook.ask.isEmpty() ) {
                
                delta += (int) ( defaultSpread / 2 );   // 25 + ...
                limPrice = Consts.NULLPRICE - delta;
            }
            else {
        
                delta += defaultSpread; // 50 + ...
                limPrice = state.orderBook.ask.first().limitPrice - delta;
            }

            // limPrice = (limPrice <= 0 ) ? 1 : limPrice;
            if (limPrice <= 0 ) {

                throw new MyException("CuiMarketMaker.generateOrder - limitPrice must be strictly positive");
            }

        }
        else {
            
            if ( state.orderBook.bid.isEmpty() ) {
                
                delta += (int) ( defaultSpread / 2 );
                limPrice = Consts.NULLPRICE + delta;
            }
            else {
                
                delta += defaultSpread;
                limPrice = state.orderBook.bid.first().limitPrice + delta;
            }
        }
/*
        return new EvtSendNewOrder( state.clock.addTime( state.clock.getCurTime(), latency ),
                new Order( this, buyOrder, true, size, limPrice, null ),
                7 );    // higher priority than EvtEmptyBook (6)
*/
        return new EvtSendNewOrder( state.eventQueue, state.clock.addTime( state.clock.getCurTime(), latency ),
                new Order( state.orderBook, this, buyOrder, true, size, limPrice, 
                    state.clock.addTime( state.clock.getCurTime(), 300000 ) ),  // expires in 10 min * 60 sec * 1000 mili = 600000
                7 );    // higher priority than EvtEmptyBook (6)

    }
}
