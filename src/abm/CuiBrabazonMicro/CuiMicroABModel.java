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
import abmlob.events.*;
import ccloop.Consts;
import ccloop.TimeStamp;

public class CuiMicroABModel {
    
    public EventDispatcher dispatcher;
    public CuiMicroMarketState state;
    
    public TimeStamp endTime;
    
    public CuiMicroABModel( ParamSet params, long randSeed ) {

        Consts.MODEL = 3;   // 'CuiBrabazon'

        Consts.PRICEDIGITS = 2;
        Consts.NULLPRICE = 30000;

        //----- holder of world clock, agents populaton, market mechanism and event queue
        
        this.state = new CuiMicroMarketState( randSeed );

        state.matchingEngine.IOC = true;

        //----- route events to handlers

        dispatcher = new EventDispatcher( );

        dispatcher.registerChannel( EvtRandomPolling.class, new HdlCuiLowFreqPolling() );
        dispatcher.registerChannel( EvtEmptyBook.class, new HdlCuiEmptyBook() );
        dispatcher.registerChannel( EvtSendNewOrder.class, new HdlCuiSendNewOrder() );
        dispatcher.registerChannel( EvtRemoveOrder.class, new HdlRemoveOrder() );
        dispatcher.registerChannel( EvtOrderExpiration.class, new HdlRemoveOrder() );
        dispatcher.registerChannel( EvtNotifyTrade.class, new HdlMicroNotifyTrade() );
        dispatcher.registerChannel( EvtNotifyQuoteChange.class, new HdlCuiNotifyQuoteChange() );

        //----- logic starts here ---------------------------

        this.endTime = new TimeStamp(1,0);
        
        state.burnInPeriod = new TimeStamp(0, 3600000);
        
        state.mu1 = params.mu1;
        state.sigma1 = params.sigma1;
        state.mu2 = params.mu2;
        state.sigma2 = params.sigma2;
        state.p1 = params.p1;
        
        state.mt = new Microtrading( state );
        
        state.mt.obiLevels = params.obiLevels;
        state.mt.obiBase = params.obiBase;
        state.mt.sizePenaltyExp = params.sizePenaltyExp;
        state.mt.isSigmaMult = params.isSigmaMult;
        state.mt.dynSigmaMult = params.dynSigmaMult;
        state.mt.alpha0 = params.alpha0;
        state.mt.alpha1 = params.alpha1;
        state.mt.alpha2 = params.alpha2;
        state.mt.beta = params.beta;
        
        state.addAgent( new CuiMicroTrader( state, 1, 0, 0, 0, true ) );  // buyer
        state.addAgent( new CuiMicroTrader( state, 1, 0, 0, 0, false ) ); // seller

        state.addAgent( new CuiMarketMaker( state, 1, 0, 0, 0 ) ); // market maker

        //----- kickstart 

        dispatcher.dispatch( new EvtEmptyBook( state.eventQueue, new TimeStamp(0,1), true ), state );     // fill bid
        dispatcher.dispatch( new EvtEmptyBook( state.eventQueue, new TimeStamp(0,1), false ), state );    // fill ask

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
