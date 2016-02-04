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
package abmlob.events;

import abmlob.orderbook.Trade;
import ccloop.TimeStamp;

public class EvtNotifyTrade extends Event {

    public Trade trade;
    
    public EvtNotifyTrade( EventQueue queue, TimeStamp eventTime, Trade trade ) {
        
        super( queue );

        this.eventTime = eventTime;
        this.trade = trade;
        this.priority = 8;  // higher priority
    }

    @Override
    public String toString() { 

        StringBuffer buf = new StringBuffer();
        
        buf.append(eventTime).append("-#").append( trade.getId() ).append("-").append(getType());
        
        return( buf.toString() );
    }

}
