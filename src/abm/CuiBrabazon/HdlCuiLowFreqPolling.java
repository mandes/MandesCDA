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
import abmlob.events.EvtRandomPolling;
import abmlob.events.Handler;
import ccloop.MarketState;

public class HdlCuiLowFreqPolling extends Handler {

    @Override
    public void broadcast(Event evt, MarketState state) {

        EvtRandomPolling rp = (EvtRandomPolling) evt;

        //---- select trader
        
        if ( state.rng.nextDouble() < 0.5 ) {   // select buy agent
            
            state.agentPop.get(0).dispatch(rp, state);
        }
        else {  // sell agent
            
            state.agentPop.get(1).dispatch(rp, state);
        }

        //---- next poll
        
        int sleep = 1;
        
        while ( state.rng.nextDouble() < 0.9847 ) { // prob of doing nothing, uniform distribution
            
            sleep++;
        }

        state.eventQueue.queue.add( 
            new EvtRandomPolling( state.eventQueue, state.clock.addTime( state.clock.getCurTime(), sleep ) ) );
    }

}
