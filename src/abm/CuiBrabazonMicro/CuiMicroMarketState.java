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

import abm.CuiBrabazon.CuiMarketState;
import abmlob.events.*;
import java.util.Arrays;

public class CuiMicroMarketState extends CuiMarketState{
    
    public Microtrading mt;
    
    public double mu1;
    public double sigma1;
    public double mu2;
    public double sigma2;
    public double p1;
    
    public CuiMicroMarketState ( long randSeed ) {
        
        super ( randSeed );
    }
    
    @Override
    public void processNextEvent ( EventDispatcher dispatcher ) {
        
        Event nextEvt = eventQueue.queue.first();
            
        eventQueue.queue.pollFirst();

        dispatcher.dispatch( nextEvt, this );            

        //---- check order book state after EvtNotifyTrade, EvtRemoveOrder, EvtOrderExpiration
        // or EvtNotifyQuoteChange: if ( nextEvt.getClass() == EvtNotifyQuoteChange.class )

        if ( Arrays.asList( EvtNotifyTrade.class, EvtRemoveOrder.class, EvtOrderExpiration.class ).contains( nextEvt.getClass() ) ) {

            if ( orderBook.bid.isEmpty() ) {

                eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, true ) ); // higher priority
            }

            if ( orderBook.ask.isEmpty() ) {

                eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, false ) );
            }
        }
    }
}
