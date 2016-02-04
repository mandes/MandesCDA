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

import abm.CuiBrabazonMicro.ParamSet;
import abmlob.events.*;
import ccloop.Consts;
import ccloop.TimeStamp;

public class CuiABModel {
    
    public EventDispatcher dispatcher;
    public CuiMarketState state;
    
    public TimeStamp endTime;
    
    public CuiABModel( ParamSet params, long randSeed ) {
        
        Consts.MODEL = 3;   // 'CuiBrabazon'
     
        Consts.PRICEDIGITS = 2;
        Consts.NULLPRICE = 30000;

        //----- holder of world clock, agents populaton, market mechanism and event queue
        
        this.state = new CuiMarketState( randSeed );

        state.matchingEngine.IOC = true;

        //----- route events to handlers

        this.dispatcher = new EventDispatcher( );

        dispatcher.registerChannel( EvtRandomPolling.class, new HdlCuiLowFreqPolling() );
        dispatcher.registerChannel( EvtEmptyBook.class, new HdlCuiEmptyBook() );
        dispatcher.registerChannel( EvtSendNewOrder.class, new HdlCuiSendNewOrder() );
        dispatcher.registerChannel( EvtRemoveOrder.class, new HdlRemoveOrder() );
        dispatcher.registerChannel( EvtOrderExpiration.class, new HdlRemoveOrder() );
        dispatcher.registerChannel( EvtNotifyTrade.class, new HdlCuiNotifyTrade() );
        dispatcher.registerChannel( EvtNotifyQuoteChange.class, new HdlCuiNotifyQuoteChange() );

        //----- logic starts here ---------------------------

        this.endTime = new TimeStamp(1,0);
        
        state.burnInPeriod = new TimeStamp(0, 3600000);
        
        state.addAgent( new CuiTrader( state, 1, 0, 0, 0, true ) );  // buyer
        state.addAgent( new CuiTrader( state, 1, 0, 0, 0, false ) ); // seller

        state.addAgent( new CuiMarketMaker( state, 1, 0, 0, 0 ) ); // market maker

        //----- kickstart 

        dispatcher.dispatch( new EvtEmptyBook( state.eventQueue, new TimeStamp(0,1), true ), state );     // fill bid
        dispatcher.dispatch( new EvtEmptyBook( state.eventQueue, new TimeStamp(0,1), false ), state );    // fill ask
        
        //dispatcher.dispatch( new EvtRandomPolling( new TimeStamp(0,1) ), state );
        state.eventQueue.queue.add( new EvtRandomPolling( state.eventQueue, new TimeStamp(0,1) ) );
    }
    
    public void run () {

        //------ cycle through events

        while ( !state.eventQueue.queue.isEmpty() 
              && ( state.eventQueue.queue.first().eventTime.day < endTime.day 
                 || ( state.eventQueue.queue.first().eventTime.day == endTime.day && state.eventQueue.queue.first().eventTime.timeTick <= endTime.timeTick ) )
              ) {
            state.processNextEvent( dispatcher );
        }
    }
}
